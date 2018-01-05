package rsstats.inventory.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import rsstats.items.SkillItem;

public class SkillSlot extends StatSlot {
    public SkillSlot(IInventory inventory, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
    }

    /**
     * Check if the stack is a valid item for this slot. Always true beside for the armor slots
     * (and now also not always true for our custom inventory slots)
     *
     * @param itemStack Предмет, который хочет поместиться в слот
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь.
     * Иначе - false.
     */
    @Override
    public boolean isItemValid(ItemStack itemStack) {
        return itemStack.getItem() instanceof SkillItem;
    }
}
