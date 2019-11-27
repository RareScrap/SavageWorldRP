package rsstats.items.perk;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.roll.RollModifier;
import rsstats.utils.LangUtils;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param entityPlayer Игрок, акивирующий перк
     */
    public void activate(EntityPlayer entityPlayer) {}

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

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        // Требования к перку добавляются в потомках

        // Дополнительная информация по кнопке Shift
        if (GuiScreen.isShiftKeyDown()) {
            list.addAll(LangUtils.translateToLocal(getUnlocalizedName() + ".description"));
        } else {
            list.addAll(LangUtils.translateToLocal(getUnlocalizedName() + ".description_short"));
            list.add( StatCollector.translateToLocal("item.StatItem.moreInfo") );
            if (canActivate()) list.add( EnumChatFormatting.GOLD + StatCollector.translateToLocal("item.PerkItem.canActivate") );
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