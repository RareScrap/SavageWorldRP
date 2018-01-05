package rsstats.inventory.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

/**
 * Слот для статов
 * @author RareScrap
 */
public class StatSlot extends Slot {
    public StatSlot(IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots
     * (and now also not always true for our custom inventory slots)
     * @param itemStack Предмет, который хочет поместиться в слот
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь. Иначе - false.
     */
    @Override
    public boolean isItemValid(ItemStack itemStack) {
        if (getStack() != null) {
            Item item = getStack().getItem();
            return item.getUnlocalizedName().equals(itemStack.getItem().getUnlocalizedName());
        } else {
            return itemStack.getItem() instanceof StatItem && !(itemStack.getItem() instanceof SkillItem);
        }
    }

    /**
     * Возвращает максимальный размер стака
     * @return 1
     */
    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    /* *
     * Returns the icon index on items.png that is used as background image of the slot.
     */
    /*@SideOnly(Side.CLIENT)
    public Icon getBackgroundIconIndex() {
        return ItemArmor.func_94602_b(this.armorType);
    }*/
}
