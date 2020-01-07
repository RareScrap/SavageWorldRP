package rsstats.items.perks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.PerkItems;
import rsstats.items.StatItems;
import rsstats.api.items.perk.PerkItem;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static rsstats.utils.LangUtils.getLocalizedName;

// TODO: Игнорировать штрафы от одного ранения
// TODO: Если персонаж поставил своей целью напиться, то на проверки смекалки и ловкости налагается штраф −2 всё время, пока он пьёт, и потом ещё d6 часов.
public class BreathOfCourage extends PerkItem {

    static {
        increasePotionsStorage();
    }
    public static final BreathOfCouragePotion potion = new BreathOfCouragePotion(24, false, 12345678);
    private static final ResourceLocation potionIcon = new ResourceLocation(RSStats.MODID,"textures/gui/container/potion_icons.png");

    public BreathOfCourage() {
        setUnlocalizedName("BreathOfCouragePerkItem");
        setTextureName(RSStats.MODID + ":perks/breath_of_courage");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(getLocalizedName(StatItems.enduranceStatItem) + " d8+");
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return super.isSuitableFor(player) && player.getStatLvl(StatItems.enduranceStatItem) >= 3;
    }

    @SubscribeEvent
    public void onDrinkingAlchogol(PlayerUseItemEvent.Finish event) {
        ExtendedPlayer player = ExtendedPlayer.get(event.entityPlayer);

        if (event.item.getItem() instanceof ItemPotion && player.hasPerk(PerkItems.breathOfCourage)) {
            player.getEntityPlayer().addPotionEffect(new DrunkEffect(potion.id, 250));
        }
    }

    private static void increasePotionsStorage() {
        Potion[] potionTypes = null;
        for (Field f : Potion.class.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a")) {
                    Field modfield = Field.class.getDeclaredField("modifiers");
                    modfield.setAccessible(true);
                    modfield.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                    potionTypes = (Potion[]) f.get(null);
                    final Potion[] newPotionTypes = new Potion[256];
                    System.arraycopy(potionTypes, 0, newPotionTypes, 0, potionTypes.length);
                    f.set(null, newPotionTypes);
                }
            } catch (Exception e) {
                System.err.println("Severe error, please report this to the mod author:");
                System.err.println(e);
            }
        }
    }

    static class DrunkEffect extends PotionEffect {
        boolean flag = false;

        public DrunkEffect(int potionId, int duration) {
            super(potionId, duration);
        }

        @Override
        public boolean onUpdate(EntityLivingBase entityLivingBase) {
            if (getDuration() > 0)
            {
                if (!flag)
                {
                    this.performEffect(entityLivingBase);
                    flag = true;
                }

                this.deincrementDuration(); // TODO: Заюзать трансформеры
            }

            return this.getDuration() > 0;
        }

        public void deincrementDuration() {
            try {
                Method deincrementDuration = this.getClass().getSuperclass().getDeclaredMethod("deincrementDuration");
                deincrementDuration.setAccessible(true);
                deincrementDuration.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void performEffect(EntityLivingBase entityLivingBase) {
            if (entityLivingBase instanceof EntityPlayer && !entityLivingBase.worldObj.isRemote) {
                ExtendedPlayer player = ExtendedPlayer.get((EntityPlayer) entityLivingBase);
                ItemStack endurance = player.getStat(StatItems.enduranceStatItem);
                System.out.println("Поднимает стату с " + endurance.getItemDamage() + " до " + (endurance.getItemDamage() < endurance.getMaxDamage() ? endurance.getItemDamage()+1 : endurance.getMaxDamage()));
                endurance.setItemDamage(endurance.getItemDamage() < endurance.getMaxDamage() ? endurance.getItemDamage()+1 : endurance.getMaxDamage());
            }
        }
    }

    // Каждому PotionEffect'у необходим Potion. Именно поэтому он тут
    static class BreathOfCouragePotion extends Potion {

        protected BreathOfCouragePotion(int id, boolean isBadEffect, int liquidColor) {
            super(id, isBadEffect, liquidColor);
            setPotionName("potion.breathOfCourage");
            setIconIndex(0, 0); // позиция по длине и высоте в текстуре с иконками
        }

        // https://forum.mcmodding.ru/threads/kak-programno-sdvinut-teksturu.23852/#post-176186
        @Override
        public boolean hasStatusIcon() {
            return false; // Предотвращаем ванильный рендер.
            // Очень надеюсь что сторонние моды догадаются что это единственный нормальный способ сделать это
            // и не будут юзать этот метод для проверки наличия у зелья иконки. Вместо этого им следует производить
            // вычисление супер-метода самостоятельно
        }

        @Override
        public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
            mc.renderEngine.bindTexture(potionIcon);
            Gui.func_146110_a(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
        }

        // Срабатывает при удалении/истечении действия зелья
        @Override
        public void removeAttributesModifiersFromEntity(EntityLivingBase p_111187_1_, BaseAttributeMap p_111187_2_, int p_111187_3_) {
            if (p_111187_1_ instanceof EntityPlayer) {
                ItemStack endurance = ExtendedPlayer.get(((EntityPlayer) p_111187_1_)).getStat(StatItems.enduranceStatItem);
                endurance.setItemDamage(endurance.getItemDamage() > 0 ? endurance.getItemDamage()-1 : 0);
            }
            super.removeAttributesModifiersFromEntity(p_111187_1_, p_111187_2_, p_111187_3_);
        }
    }
}
