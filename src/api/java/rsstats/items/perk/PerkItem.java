package rsstats.items.perk;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.roll.RollModifier;
import rsstats.utils.DescriptionCutter;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;

import java.util.List;
import java.util.Map;

public abstract class PerkItem extends Item {

    public PerkItem() {
        this.setCreativeTab(RSStats.CREATIVE_TAB);
        this.setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    // абстрактный, т.к. наследник вероятнее всего удалит какой-нибудь Requirement из супера. А возвращать null слишком уродливо и бессмсленно по большому счету
    public abstract List<Requirement> getRequirements();

    public abstract Map<IModifierDependent, RollModifier> getModifiers();

    public void activate(EntityPlayer entityPlayer) {
        // TODO: Не тестировалось
    }

    public boolean isSuitableFor(ExtendedPlayer player) {
        for (Requirement requirement : getRequirements()) {
            if (!requirement.isSuitableFor(player))
                return false;
        }

        return true;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        for (Requirement requirement : getRequirements()) {
            list.add(requirement.toStringTranslated());
        }

        list.add("");

        // Дополнительная информация по кнопке Shift
        if (GuiScreen.isShiftKeyDown()) {
            String[] str = DescriptionCutter.cut(4, StatCollector.translateToLocal(getUnlocalizedName() + ".description"));
            for (int i = 0; i < str.length; i++)
                list.add( str[i] );

            list.add("");
        } else {
            list.add(StatCollector.translateToLocal(getUnlocalizedName() + ".description_short"));
            list.add( StatCollector.translateToLocal("item.StatItem.moreInfo") );
        }
    }

    // Не предполагаю использования инвентаря для перков, отличного от TabInventory. Поэтому и использую евент StackAddToTabEvent
    @SubscribeEvent
    public void onInventoryChanged(StackAddToTabEvent e) { //TODO: Изобрести общий евент для изменения любых инвентарей
        //if (e.entity.worldObj.isRemote)
        //return;

        // TODO: Перк все равно добавится, даже если его добавть в другую вкладку.  Т.е. нет проверки на имя вкладки
        /* Если использовать instanceof PerkItem, то onAdd сработает для ВСЕХ перков-наследников. Т.е. нельзя
         * использовать instanceof this, то я испольльзую вот этот хак: */
        if (e.change.actualItemStack != null && e.change.actualItemStack.getItem().getClass() == this.getClass())
            onAdd(e);

        if (e.change.currentItemStack != null && e.change.currentItemStack.getItem().getClass() == this.getClass())
            onRemove(e);
    }

    public void onAdd(StackAddToTabEvent e) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(e.entityPlayer);
        extendedPlayer.modifierManager.addModifiers(getModifiers());
        extendedPlayer.updateParams();
    }

    public void onRemove(StackAddToTabEvent e) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(e.entityPlayer);
        extendedPlayer.modifierManager.removeModifiers(getModifiers());
        extendedPlayer.updateParams();
    }
}