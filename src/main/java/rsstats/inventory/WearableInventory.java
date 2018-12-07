package rsstats.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.data.ExtendedPlayer;

public class WearableInventory implements IInventory {
    private final String name = "Wearable";
    /** The key used to store and retrieve the inventory from NBT */
    private static final String NBT_TAG = "wearable";

    private static final int INV_SIZE = 16;
    private ItemStack[] inventory = new ItemStack[INV_SIZE];
    private static final int STACK_LIMIT = 1;
    private ExtendedPlayer extendedPlayer;

    /**
     * Необходимый пустой публичный контсруктор
     */
    public WearableInventory() {}

    public WearableInventory(ExtendedPlayer extendedPlayer) {
        this.extendedPlayer = extendedPlayer;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     *
     * @param slotIndex
     */
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
    }

    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     *
     * @param slotIndex Слот в инвенторе, где лежит предмет, стак которого нужно уменьшить
     * @param amount На сколько нужно уменьший стак
     * @return Предмет с уменьшенным стаком
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);
        if (stack != null) {
            if (stack.stackSize > amount) {
                stack = stack.splitStack(amount);
                // ВАЖНО this.onInventoryChanged();
            }
            else {
                setInventorySlotContents(slotIndex, null);
            }
        }
        return stack;
    }

    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     *
     * @param slotIndex
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        /*ItemStack stack = getStackInSlot(slotIndex);
        setInventorySlotContents(slotIndex, null);
        return stack;*/
        return null;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param slotIndex
     * @param itemStack
     */
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        // Работаем с модификаторами предмета
        if (itemStack != null) { // извлекаем и сохраняем модификаторы
            extendedPlayer.modifierManager.addModifiersFrom(itemStack);
        }

        // Если до этого в слоте находился другой предмет - удаляем его модификаторы
        if (getStackInSlot(slotIndex) != null) {
            extendedPlayer.modifierManager.removeModifiersFrom(getStackInSlot(slotIndex));
        }

        // Непосредственно помещаем сам стак в слот
        this.inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }

        // TODO: ВАЖНО this.onInventoryChanged();
    }

    /**
     * Returns the name of the inventory
     */
    @Override
    public String getInventoryName() {
        return name;
    }

    /**
     * Returns if the inventory is named
     */
    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    /**
     * Returns the maximum stack size for a inventory slot.
     */
    @Override
    public int getInventoryStackLimit() {
        return STACK_LIMIT;
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void markDirty() {

    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     *
     * @param p_70300_1_
     */
    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
     *
     * @param p_94041_1_
     * @param p_94041_2_
     */
    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return true;
    }

    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }

        // We're storing our items in a custom tag list using our 'NBT_TAG' from above
        // to prevent potential conflicts
        compound.setTag(NBT_TAG, items);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");

            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }
    }
}
