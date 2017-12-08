/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 *
 * @author rares
 */
public class StatsInventory implements IInventory {
    /** The name your custom inventory will display in the GUI, possibly just "Inventory" */
    // TODO: Локализировать эту строку
    private final String name = "Stats";

    /** The key used to store and retrieve the inventory from NBT */
    private final String tagName = "stats";

    /** Define the inventory size here for easy reference */
    /* This is also the place to define which slot is which if you have different types,
     * for example SLOT_SHIELD = 0, SLOT_AMULET = 1; */
    public static final int INV_SIZE = 9;
    /** Масимальный размер стака для предметов в инвенторе {@link #inventory} */
    private static final int STACK_LIMIT = 1;

    /** Inventory's size must be same as number of slots you add to the Container class */
    private ItemStack[] inventory = new ItemStack[INV_SIZE];

    /** Необходимый пустой публичный контсруктор */
    public StatsInventory() {
        System.out.print("StatsInventory()\n");
    }
    
    /**
     * Геттер для {@link #inventory}
     * @return Размер массива {@link #inventory}
     */
    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    /**
     * Геттер для получения элементов из инвентаря {@link #inventory}
     * @param slotIndex Индекс слота в инвенторе, из которого нужно получить предмет
     * @return Предмет под индексом slotIndex в инвенторе
     */
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
    }

    //TODO: Когда вызывается этот метод и зачем он нужен?
    /**
     * Уменьшает размер стака предмета
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

    //TODO: Когда вызывается этот метод и зачем он нужен?
    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        ItemStack stack = getStackInSlot(slotIndex);
        setInventorySlotContents(slotIndex, null);
        return stack;
    }

    //TODO: Когда вызывается этот метод и зачем он нужен?
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        this.inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
        // TODO: ВАЖНО this.onInventoryChanged();
    }

    /**
     * Геттер для {@link #name}
     * @return Имя инвентаря
     */
    @Override
    public String getInventoryName() {
        return name;
    }
    
    /*
    // Было в туториале но зачем - хз
    @Override
    public boolean isInvNameLocalized() {
        return name.length() > 0;
    }
    @Override
    public void onInventoryChanged()
    {
            for (int i = 0; i < getSizeInventory(); ++i)
            {
                    if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
                            inventory[i] = null;
                    }
            }
    }*/

    @Override
    public boolean hasCustomInventoryName() {
        // TODO Просто чтоб игра не вылетала
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Возвращает масимальный размер стака для предметов в этом инвенторе
     * @return {@link #STACK_LIMIT}
     */
    @Override
    public int getInventoryStackLimit() {
        return STACK_LIMIT;
    }

    @Override
    public void markDirty() {
        // TODO Просто чтоб игра не вылетала
        return;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Инвентарь может использоваться игроком?
     * @param entityPlayer Сущность игрока, взаимодействующая с инвентарем
     * @return false
     */
    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return true/*false*/;
    }   

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    /**
     * Проверяет, можно ли поместить предмет в данный слот инвентаря {@link #inventory}
     * 
     * This method doesn't seem to do what it claims to do, as
     * items can still be left-clicked and placed in the inventory
     * even when this returns false
     * @param slotIndex TODO
     * @param itemStack Предмет, который хочет поместиться в инвентарь
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь.
     * Иначе - false.
     */
    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        // If you have different kinds of slots, then check them here:
        // if (slot == SLOT_SHIELD && itemstack.getItem() instanceof ItemShield) return true;

        // TODO: Класс StatItem должен импортироваться из другого мода
        // return itemStack.getItem() instanceof StatItem;
        
        // Пока в инвентаре может храниться любой предмет
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

        // We're storing our items in a custom tag list using our 'tagName' from above
        // to prevent potential conflicts
        compound.setTag(tagName, items);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(tagName, Constants.NBT.TAG_LIST);

        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");

            if (slot >= 0 && slot < getSizeInventory()) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }  
}
