package rsstats.inventory;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.common.RSStats;
import rsstats.items.SkillItem;

import java.util.ArrayList;

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

    /** Структура, хранящая предметы, которые будут отображены в инвентаре пользователя.
     * Inventory's size must be same as number of slots you add to the Container class. */
    private ItemStack[] inventory = new ItemStack[INV_SIZE];

    /** Структура, хранящая предметы инвентаря в стаках */
    private ArrayList<ItemStack> skills = new ArrayList<ItemStack>();

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

        if (itemStack == null) { // Очистить слот
            if (inventory[slotIndex] != null) { // Был ли слот очищенным до этого?
                // Если нет - удаляем то, что есть сейчас в слоте из хранилища скиллов
                removeSkill(inventory[slotIndex].getUnlocalizedName());
            }

            // Обновляем сам слот
            this.inventory[slotIndex] = itemStack;
        } else { // Добавить новый стак в слот
            if (inventory[slotIndex] /*!*/== null && containSkill(itemStack.getUnlocalizedName())) {
                removeSkill(itemStack.getUnlocalizedName());
            }
            this.inventory[slotIndex] = itemStack;
            skills.add(itemStack);
        }

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

        for (int i = 0; i < skills.size(); ++i) {
            if (skills.get(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                //item.setByte("Slot", (byte) i);
                skills.get(i).writeToNBT(item);
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
            // TODO: Исправить отвратительную инициализацию
            // TODO: При самом первом запуске начальные предметы не создаются
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "ClimbingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "EquitationSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "LockpickingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "DrivingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "FightingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "DisguiseSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "ThrowingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "PilotingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "SwimmingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "ShootingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "ShippingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "GamblingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "PerceptionSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "SurvivalSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "TrackingSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "MedicineSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "ProvocationSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "InvestigationSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "RepearSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "StreetFlairSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "IntimidationSkillItem"), 1, 2));
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, "DiplomacySkillItem"), 1, 2));

            for (ItemStack skill : skills) {
                if (skill != null) {
                    NBTTagCompound item = new NBTTagCompound();
                    skill.writeToNBT(item);
                    items.appendTag(item);
                }
            }
            compound.setTag(NBT_TAG, items);
        }


        byte slot = 0;
        String asd = ((SkillItem) ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(0)).getItem()).parentStat.getUnlocalizedName();
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound NBTItem = (NBTTagCompound) items.getCompoundTagAt(i);
            //byte slot = NBTItem.getByte("Slot");

            SkillItem item = (SkillItem) ItemStack.loadItemStackFromNBT(NBTItem).getItem();

            if (!containSkill(item.getUnlocalizedName()))
                skills.add(ItemStack.loadItemStackFromNBT(NBTItem));

            if (slot >= 0 && slot < getSizeInventory() && item.parentStat.getUnlocalizedName().equals(asd)) {
                ItemStack itemstack = skills.get(skills.size()-1);
                inventory[slot++] = itemstack;
            }
        }
    }

    /**
     * Очищает {@link #inventory}, выставляя все его элементы null
     */
    private void clearInventory() {
        for (int i = 0; i < getSizeInventory(); i++) {
            inventory[i] = null;
        }

    }

    /**
     * Проверяет, содержится ли в {@link #skills} стак с предметом по имение skillName
     * @param skillName UnlocalizedName поискового скилла
     * @return True, если элемент есть в {@link #skills}, иначе - false.
     */
    private boolean containSkill(String skillName) {
        for (ItemStack skill : skills) {
            if (skill.getUnlocalizedName().equals(skillName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Находит и удаляет стак из {@link #skills}
     * @param unlocalizedSkillName UnlocalizedName, по которому будет произведен поиск.
     *                             Если элемент найтен - он удалится из {@link #skills}
     */
    private void removeSkill(String unlocalizedSkillName) {
        for (ItemStack skill : skills) {
            if (skill.getUnlocalizedName().equals(unlocalizedSkillName)) {
                skills.remove(skill);
                return;
            }
        }
    }

    /**
     * Заполняет {@link #inventory} подходящими элементами из {@link #skills}
     * @param parentStatName {@link #inventory} будет заполнен только теми элентами, которые
     *                        имеют данный parentStat.UnlocalizedName
     */
    public void setSkillsFor(String parentStatName) {
        clearInventory();
        int slot = 0;
        for (ItemStack skill : skills) {
            SkillItem item = (SkillItem) skill.getItem();
            if (parentStatName.equals(item.parentStat.getUnlocalizedName()))
                this.inventory[slot++] = skill;
                //setInventorySlotContents(slot++, skill);
        }
    }
}
