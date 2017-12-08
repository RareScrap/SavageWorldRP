package rsstats.inventory.slots;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Слот для статов
 * @author RareScrap
 */
public class StatSlot extends Slot {
    /**
     * TODO
     * @param inventory
     * @param slotIndex
     * @param x
     * @param y 
     */
    public StatSlot(IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots
     * (and now also not always true for our custom inventory slots)
     * @param stack Предмет, который хочет поместиться в слот
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь.
     * Иначе - false.
     */
    @Override
    public boolean isItemValid(ItemStack stack) {
        // TODO: Класс StatItem должен импортироваться из другого мода
        // return itemStack.getItem() instanceof StatItem;
        
        // Пока в инвентаре может храниться любой предмет
        return true;
    }

    /**
     * Возвращает максимальный размер стака
     * @return 1
     */
    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    /**
     * Returns the icon index on items.png that is used as background image of the slot.
     */
    /*@SideOnly(Side.CLIENT)
    public Icon getBackgroundIconIndex() {
        return ItemArmor.func_94602_b(this.armorType);
    }*/

}
