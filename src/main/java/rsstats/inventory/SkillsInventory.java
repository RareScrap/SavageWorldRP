package rsstats.inventory;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.items.SkillItem;

import java.util.ArrayList;

public class SkillsInventory extends StatsInventory {
    /** The key used to store and retrieve the inventory from NBT */
    private static final String NBT_TAG = "skills";

    /** Define the inventory size here for easy reference */
    /* This is also the place to define which slot is which if you have different types,
     * for example SLOT_SHIELD = 0, SLOT_AMULET = 1; */
    private static final int INV_SIZE = 27;

    /** Структура, хранящая только те предметы, которые будут отображены в инвентаре пользователя.
     * Inventory's size must be same as number of slots you add to the Container class. */
    private ItemStack[] inventory = new ItemStack[INV_SIZE];

    /** Структура, хранящая все предметы инвентаря в стаках */
    private ArrayList<ItemStack> skills = new ArrayList<ItemStack>();

    /**
     * Необходимый публичный контсруктор
     */
    public SkillsInventory(EntityPlayer entityPlayer) {
        super(entityPlayer);
    }

    /**
     * Геттер для размера {@link #inventory}
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
            if (/*inventory[slotIndex]  *!* == null &&*/ containSkill(itemStack.getUnlocalizedName())) {
                removeSkill(itemStack.getUnlocalizedName());
            } // TODO: !!!!!!!!!!!!!!!!!!!!!!!!
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
        return itemStack.getItem() instanceof SkillItem;
    }

    /**
     * Возвращает локализованное имя инвентаря
     * @return Имя инвентаря
     */
    @Override
    public String getInventoryName() {
        return StatCollector.translateToLocal("inventory." + NBT_TAG);
    }

    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (ItemStack skill : skills) {
            if (skill != null) {
                NBTTagCompound item = new NBTTagCompound();
                //item.setByte("Slot", (byte) i);
                skill.writeToNBT(item);
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
        NBTTagList items = compound.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        /* Сравнивать compound'ы через строки понадобвится того, когда потребуется чтобы статы не
         * добавлялись когда их нет, но в compound есть тег инвентаря статов */
        //String a = compound.toString();
        //String b = a.replaceFirst(NBT_TAG, "");

        if (items.tagCount() == 0) {
            initItems();
            return;
        }

        // Штатное чтение из NBT
        byte slot = 0;
        String asd = ((SkillItem) ItemStack.loadItemStackFromNBT(items.getCompoundTagAt(0)).getItem()).parentStat.getUnlocalizedName();
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound NBTItem = items.getCompoundTagAt(i);
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
     * Инициализирует начальные скиллы
     */
    @Override
    public void initItems() {
        // Заполняем общее хранилище
        for (CommonProxy.Skills skill : CommonProxy.Skills.values()) {
            skills.add(new ItemStack(GameRegistry.findItem(RSStats.MODID, skill.toString())));
        }

        // Заполняем хранилище отображения
        for (int i = 0, slot = 0; i < skills.size() && slot < inventory.length; i++) {
            SkillItem item = (SkillItem) skills.get(i).getItem();
            if (item.parentStat.getUnlocalizedName().equals("item.StrengthStatItem")) {
                inventory[slot++] = skills.get(i);
            }
        }

        markDirty();
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
     * Получаем стак из {@link #skills} по указанному UnlocalizedName
     * @param unlocalizedSkillName UnlocalizedName нужного стака скилла
     * @return Стак {@link SkillItem}
     */
    public ItemStack getSkill(String unlocalizedSkillName) {
        for (ItemStack skill : skills) {
            if (skill.getUnlocalizedName().equals(unlocalizedSkillName)) {
                return skill;
            }
        }
        return null;
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

    /**
     * Очищает все имеющиеся хранилища {@link ItemStack}'ов
     */
    @Override
    public void totalClear() {
        // TODO: Почему нельзя вызвать супер?
        skills.clear();
        clearInventory();
    }

    /**
     * Устанавливает новое содержимое для хранилища скиллов ({@link #skills})
     * @param list Обновленное хранилие скиллов
     */
    public void setNewSkills(ArrayList<ItemStack> list) {
        this.skills = list;
        if (Minecraft.getMinecraft().currentScreen != null) {
            Minecraft.getMinecraft().currentScreen.updateScreen();
        }
    }

    public ArrayList<ItemStack> getSkills() {
        return skills;
    }
}
