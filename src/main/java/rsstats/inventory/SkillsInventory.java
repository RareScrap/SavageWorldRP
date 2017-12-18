package rsstats.inventory;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.common.RSStats;
import rsstats.items.SkillItem;

public class SkillsInventory extends StatsInventory {
    /** The name your custom inventory will display in the GUI, possibly just "Inventory" */
    // TODO: Локализировать эту строку
    private final String name = "Skills";

    /** The key used to store and retrieve the inventory from NBT */
    private static final String NBT_TAG = "skills";

    /** Define the inventory size here for easy reference */
    /* This is also the place to define which slot is which if you have different types,
     * for example SLOT_SHIELD = 0, SLOT_AMULET = 1; */
    private static final int INV_SIZE = 27;
    /** Масимальный размер стака для предметов в инвенторе {@link #inventory} */
    private static final int STACK_LIMIT = 1;

    /** Структура, хранящая предметы инвентаря в стаках.
     * Inventory's size must be same as number of slots you add to the Container class. */
    private ItemStack[] inventory = new ItemStack[INV_SIZE];

    /**
     * Необходимый пустой публичный контсруктор
     */
    public SkillsInventory() {}

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
     * @return Стак предметов под индексом slotIndex в инвенторе
     */
    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory[slotIndex];
    }

    /**
     * Уменьшает размер стака предмета
     *
     * @param slotIndex Слот в инвенторе, где лежит предмет, стак которого нужно уменьшить
     * @param amount    На сколько нужно уменьший стак
     * @return Предмет с уменьшенным стаком
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        // Можно ли вызывать супер? Пока я просто скопирую супер сюда
        // getStackInSlot в супере вызовется для инвенторя в супере или для инвенторя тут?
        // return super.decrStackSize(slotIndex, amount);

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

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        //super.setInventorySlotContents(slotIndex, itemStack);

        this.inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
        // TODO: ВАЖНО this.onInventoryChanged();
    }

    /**
     * Проверяет, можно ли поместить предмет в данный слот инвентаря {@link #inventory}
     * <p>
     * This method doesn't seem to do what it claims to do, as
     * items can still be left-clicked and placed in the inventory
     * even when this returns false
     *
     * @param slotIndex TODO
     * @param itemStack Предмет, который хочет поместиться в инвентарь
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь.
     * Иначе - false.
     */
    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        boolean a = itemStack.getItem() instanceof SkillItem;
        return itemStack.getItem() instanceof SkillItem;
    }

    /**
     * Возвращает масимальный размер стака для предметов в этом инвенторе
     *
     * @return {@link #STACK_LIMIT}
     */
    @Override
    public int getInventoryStackLimit() {
        return STACK_LIMIT;
    }

    /**
     * Записывает состояние инвентаря в NBT
     *
     * @param compound TODO
     */
    @Override
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
     *
     * @param compound TODO
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        //super.readFromNBT(compound);

        NBTTagList items = compound.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        if (items.tagCount() == 0) {
            // TODO: Инициализировать базовый навыки
        }

        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = (NBTTagCompound) items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");

            if (slot >= 0 && slot < getSizeInventory()) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }

    /**
     * Очищает {@link #inventory}, выставляя все его элементы null
     */
    public void clearInventory() {
        for (int i = 0; i < getSizeInventory(); i++) {
            inventory[i] = null;
        }

    }

    public void setSkillsFor(String parentStatName) {
        clearInventory();

        if ("item.StrengthStatItem".equals(parentStatName)) {
            setInventorySlotContents(0, new ItemStack(GameRegistry.findItem(RSStats.MODID, "ClimbingSkillItem"), 1, 2));
        } else if ("item.AgilityStatItem".equals(parentStatName)) {
            setInventorySlotContents(0, new ItemStack(GameRegistry.findItem(RSStats.MODID, "EquitationSkillItem"), 1, 2));
            setInventorySlotContents(1, new ItemStack(GameRegistry.findItem(RSStats.MODID, "LockpickingSkillItem"), 1, 2));
            setInventorySlotContents(2, new ItemStack(GameRegistry.findItem(RSStats.MODID, "DrivingSkillItem"), 1, 2));
            setInventorySlotContents(3, new ItemStack(GameRegistry.findItem(RSStats.MODID, "FightingSkillItem"), 1, 2));
            setInventorySlotContents(4, new ItemStack(GameRegistry.findItem(RSStats.MODID, "DisguiseSkillItem"), 1, 2));
            setInventorySlotContents(5, new ItemStack(GameRegistry.findItem(RSStats.MODID, "ThrowingSkillItem"), 1, 2));
            setInventorySlotContents(6, new ItemStack(GameRegistry.findItem(RSStats.MODID, "PilotingSkillItem"), 1, 2));
            setInventorySlotContents(7, new ItemStack(GameRegistry.findItem(RSStats.MODID, "SwimmingSkillItem"), 1, 2));
            setInventorySlotContents(8, new ItemStack(GameRegistry.findItem(RSStats.MODID, "ShootingSkillItem"), 1, 2));
            setInventorySlotContents(9, new ItemStack(GameRegistry.findItem(RSStats.MODID, "ShippingSkillItem"), 1, 2));
        } else if ("item.IntelligenceStatItem".equals(parentStatName)) {
            setInventorySlotContents(0, new ItemStack(GameRegistry.findItem(RSStats.MODID, "GamblingSkillItem"), 1, 2));
            setInventorySlotContents(1, new ItemStack(GameRegistry.findItem(RSStats.MODID, "PerceptionSkillItem"), 1, 2));
            setInventorySlotContents(2, new ItemStack(GameRegistry.findItem(RSStats.MODID, "SurvivalSkillItem"), 1, 2));
            setInventorySlotContents(3, new ItemStack(GameRegistry.findItem(RSStats.MODID, "TrackingSkillItem"), 1, 2));
            setInventorySlotContents(4, new ItemStack(GameRegistry.findItem(RSStats.MODID, "MedicineSkillItem"), 1, 2));
            setInventorySlotContents(5, new ItemStack(GameRegistry.findItem(RSStats.MODID, "ProvocationSkillItem"), 1, 2));
            setInventorySlotContents(6, new ItemStack(GameRegistry.findItem(RSStats.MODID, "InvestigationSkillItem"), 1, 2));
            setInventorySlotContents(7, new ItemStack(GameRegistry.findItem(RSStats.MODID, "RepearSkillItem"), 1, 2));
            setInventorySlotContents(8, new ItemStack(GameRegistry.findItem(RSStats.MODID, "StreetFlairSkillItem"), 1, 2));
        } else if ("item.EnduranceStatItem".equals(parentStatName)) {

        } else if ("item.CharacterStatItem".equals(parentStatName)) {
            setInventorySlotContents(0, new ItemStack(GameRegistry.findItem(RSStats.MODID, "IntimidationSkillItem"), 1, 2));
            setInventorySlotContents(1, new ItemStack(GameRegistry.findItem(RSStats.MODID, "DiplomacySkillItem"), 1, 2));
        }
    }
}
