package rsstats.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.blocks.UpgradeStationBlock;

/**
 * Инвентарь, встроенные в блок {@link UpgradeStationBlock}
 */
public class UpgradeStationInventory implements IInventory {
    private final String name = "UpgradeStation";
    /** The key used to store and retrieve the inventory from NBT */
    private static final String NBT_TAG = "upgrade_station";

    private static final int INV_SIZE = 27;
    private ItemStack[] inventory = new ItemStack[INV_SIZE];
    private static final int STACK_LIMIT = 1;

    //UpgradeStationEntity upgradeStationEntity;

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory() {
        return INV_SIZE;
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
     * Удаляет предмет из слота инвентаря до определенного количества элементов и возвращает их в новый стак.
     * @param slotIndex Слот в инвенторе, где лежит предмет, стак которого нужно уменьшить
     * @param amount До скольки нужно уменьший стак
     * @return Предмет с уменьшенным стаком
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);
        if (stack != null) {
            if (stack.stackSize > amount) {
                stack = stack.splitStack(amount);
                markDirty(); // Аналог onInventoryChanged()
            } else {
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
        ItemStack stack = getStackInSlot(slotIndex);
        setInventorySlotContents(slotIndex, null);
        return stack;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     *
     * @param slotIndex
     * @param itemStack
     */
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        this.inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
        markDirty();
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
        // TODO: ХЗ что это мы делаем. Удаляем мусор? Гарантированно очищаем слоты?
        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
                inventory[i] = null;
            }
        }

        // TODO: Проверить при помощи NBTEdit как мод ведет себя без этой строки
        //writeToNBT(upgradeStationEntity.getEntityData());
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

        for (int i = 0; i < getSizeInventory(); i++) {
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

        // Штатное чтение из NBT
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }
}
