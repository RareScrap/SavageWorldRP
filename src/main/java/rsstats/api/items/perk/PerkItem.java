package rsstats.api.items.perk;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import rsstats.api.roll.RollModifier;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.utils.LangUtils;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rsstats.utils.Utils.formatCooldownTime;

public class PerkItem extends Item {

    public PerkItem() {
        this.setCreativeTab(RSStats.CREATIVE_TAB);
        this.setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public Map<IModifierDependent, RollModifier> getModifiers() {
        return new HashMap<IModifierDependent, RollModifier>(); // TODO: null?
    }

    /**
     * Вызывается, если игрок активирует данный перк
     * @param player Игрок, акивирующий перк
     */
    public void activate(ExtendedPlayer player) {
        int cooldown = getCooldown(player);
        if (cooldown > 0) player.cooldownManager.setCooldown(this, cooldown);
    }

    /**
     * Определяет, является ли перк акивируемым
     * @return True, если перк можно активироть. Иначе - false.
     */
    public boolean canActivate() {
        return false;
    }

    public boolean isSuitableFor(ExtendedPlayer player) {
        return player.rank.moreOrEqual(ExtendedPlayer.Rank.NOVICE);
    }

    /**
     * Вычисляет кулдаун этого перка для указанного игрока.
     * Обратите внимание, что этот метод расчитывает время для
     * полного восстановления перка. Для получения оставшегося времени
     * кулдауна см. {@link rsstats.data.CooldownManager#getCooldown(PerkItem)}
     * @param player Игрок, для которого расчитывается кулдаун
     * @return Время полного восстановления перка
     * @see rsstats.data.CooldownManager#getCooldown(PerkItem)
     */
    public int getCooldown(ExtendedPlayer player) {
        return 0;
    }

    /**
     * Проверят, имеет ли перк кулдаун.
     * Обратите внимание что если вам нужно проверить наличии кулдауна,
     * чтобы потом его использовать, то лучше реализуйте эту проверку через
     * {@link #getCooldown(ExtendedPlayer)}, чтобы избежать повторного
     * вызова getCooldown(ExtendedPlayer), который потенциально может
     * снижать произвоительность.
     * @param player Игрок, для перка которого производится проверка
     * @return True, если время восстановления > 0. Иначе - false.
     */
    public boolean hasCooldown(ExtendedPlayer player) {
        return getCooldown(player) > 0;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        // Требования к перку добавляются в потомках

        // Дополнительная информация по кнопке Shift
        if (GuiScreen.isShiftKeyDown()) {
            list.addAll(LangUtils.translateToLocal(getUnlocalizedName() + ".description"));
        } else {
            list.addAll(LangUtils.translateToLocal(getUnlocalizedName() + ".description_short"));
            list.add( StatCollector.translateToLocal("item.StatItem.moreInfo") );

            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(player);
            int cooldown = extendedPlayer.cooldownManager.getCooldown(this);
            if (hasCooldown(extendedPlayer) && cooldown > 0) {
                list.add(EnumChatFormatting.GOLD +
                        StatCollector.translateToLocalFormatted("item.PerkItem.cooldown", formatCooldownTime(cooldown)));
            } else if (canActivate())
                list.add( EnumChatFormatting.GOLD + StatCollector.translateToLocal("item.PerkItem.canActivate") );
        }
    }

    // Не предполагаю использования инвентаря для перков, отличного от TabInventory. Поэтому и использую евент StackAddToTabEvent
    @SubscribeEvent
    public void onInventoryChanged(StackAddToTabEvent.Pre e) { //TODO: Изобрести общий евент для изменения любых инвентарей
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

    public void onAdd(StackAddToTabEvent.Pre e) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(e.entityPlayer);
        extendedPlayer.modifierManager.addModifiers(getModifiers());
        //extendedPlayer.updateParams(); // TODO: Т.к. модификаторы влияют только на роллы и не могут влиять на уровень статов, то пересчитывать параметры нет нужды. Но это только пока!
        extendedPlayer.sync();
    }

    public void onRemove(StackAddToTabEvent.Pre e) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(e.entityPlayer);
        extendedPlayer.modifierManager.removeModifiers(getModifiers());
        //extendedPlayer.updateParams();
        extendedPlayer.sync();
    }
}
