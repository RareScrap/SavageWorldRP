package rsstats.inventory.tabs_inventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.utils.HashMapUtils;

import java.util.HashMap;
import java.util.Random;

/**
 * Инвентарь, хранящий свое содержимое в нескольких раздельных массивах одинакового размера по принципу вкладок
 */
public class TabInventory implements IInventory {
    /** Имя инвентаря, используемое для сохранения содержимого в NBT */
    private final String inventoryName;

    /** Вместимость каждой вкладки */
    private int tabSlotsCount;
    /** Имя текущей откртой вкладки */
    private String currentTabKey;
    /** Хранилище вкладок с предметами */
    private HashMap<String, Tab> items = new HashMap<String, Tab>();

    /** Сущность, к которой привязан инвентарь */
    private Entity inventoryOwnerEntity; // TODO: Добавить такое же поле к TabHostInventory

    /**
     * Конструктор, создающий инвентарь с указанными параметрами. После взова конструктора, вам
     * следует добавить в инвентарь вкладки вызовом метода {@link #addTab(String)}.
     * @param inventoryName Имя инвентарая, которое будет использовано при сохранении его содержимого в NBT
     * @param tabSlotsCount Вместимость каждой вкладки
     */
    public TabInventory(String inventoryName, int tabSlotsCount, Entity inventoryOwnerEntity) {
        this.inventoryName = inventoryName;
        this.tabSlotsCount = tabSlotsCount;
        this.inventoryOwnerEntity = inventoryOwnerEntity;
    }

    @Override
    public int getSizeInventory() {
        return tabSlotsCount;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (items.isEmpty()) {
            throw new RuntimeException("TabInventory hasn't any tab");
        }

        if (slotIndex >= 0 && slotIndex < getSizeInventory()) {
            if (this.items.get(currentTabKey) != null) {
                return this.items.get(currentTabKey).stacks[slotIndex];
            }
        }

        return null;
    }

    /**
     * Уменьшаем размер стака до определенного количества элементов и возвращаем получившийся стак.
     * @param slotIndex Номер слота в инвенторе, где лежит предмет, стак которого нужно уменьшить
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

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return null; // Отключаем выбрасывание при закрытии GUI
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        // Проверка на превышения лимита размера стака
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }

        // Добавляем стак в хранилище
        items.get(currentTabKey).stacks[slotIndex] = itemStack;

        //  Уведомляем об изменении инвентаря
        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return inventoryName;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false; // TODO: Не понимаю что этот метод делает. Если он вернет true - то откуда он возьмем этот самй CustomInventoryName?
    }

    @Override
    public int getInventoryStackLimit() {
        return items.get(currentTabKey).stackLimit;
    }

    // http://www.minecraftforge.net/forum/topic/44810-111-how-does-notifyblockupdate-amp-markdirty-work/#comment-242622
    // http://www.minecraftforge.net/forum/topic/47811-what-is-the-function-of-markdirty/
    @Override
    public void markDirty() {
        // Чистим стаки с пустыми предметами
        for (Tab tab : items.values()) {
            ItemStack[] stacks = tab.stacks;

            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] != null && stacks[i].stackSize == 0) {
                    tab.stacks[i] = null;
                }
            }
        }

        // TODO: Проверить при помощи NBTEdit как мод ведет себя без этой строки
        // Сохраняем изменившиеся данные
        writeToNBT(inventoryOwnerEntity.getEntityData());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return entityPlayer.capabilities.isCreativeMode; // Инвентарь можно изменять, только если юзер в креативе
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        return true;
        //return items.get(currentTabKey).isItemValidForSlotInTab(slotIndex, itemStack);
    }









    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList tabsTagList = new NBTTagList();

        for (Tab tab : items.values()) {
            NBTTagCompound tabCompound = new NBTTagCompound();
            tabCompound.setString("tab_name", HashMapUtils.getKeyByValue(items, tab));

            NBTTagList tabItemsList = new NBTTagList();
            for (int i = 0; i < tab.stacks.length; i++) {
                if (tab.stacks[i] != null) {
                    NBTTagCompound item = new NBTTagCompound();
                    item.setByte("Slot", (byte) i);
                    tab.stacks[i].writeToNBT(item);
                    tabItemsList.appendTag(item);
                }
            }
            tabCompound.setTag("tab_itemstacks", tabItemsList);

            tabsTagList.appendTag(tabCompound);
        }

        // We're storing our items in a custom tag list using our 'NBT_TAG' from above
        // to prevent potential conflicts
        compound.setTag(inventoryName, tabsTagList);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(inventoryName, Constants.NBT.TAG_COMPOUND);

        /* Если инвентарь статов пустой или не содержвится в пришедшем compound'е (а он скорее всего содержится, см init())
         * - добавляем стандартный набор статов */
        /*if (items.tagCount() == 0) {
            initItems();
            return;
        }*/

        // Штатное чтение из NBT
        NBTTagList tabsTagList = compound.getTagList(inventoryName, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tabsTagList.tagCount(); i++) {
            NBTTagCompound tabCompound = tabsTagList.getCompoundTagAt(i);
            String tabName = tabCompound.getString("tab_name");

            NBTTagList tabItemsList = tabCompound.getTagList("tab_itemstacks", Constants.NBT.TAG_COMPOUND);
            ItemStack[] itemStacks = new ItemStack[tabSlotsCount];
            for (int i1 = 0; i1 < tabItemsList.tagCount(); i1++) {
                NBTTagCompound item = tabItemsList.getCompoundTagAt(i1);
                byte slot = item.getByte("Slot");
                if (slot >= 0 && slot < getSizeInventory()) {
                    itemStacks[i1] = ItemStack.loadItemStackFromNBT(item); // TODO: Пофиксить баг со "сползающим" инвентарем
                }
            }
            addTab(tabName, itemStacks);
        }
    }

    public void setCurrentTab(String newCurrentTabName) {
        // TODO: защита от дурака
        this.currentTabKey = newCurrentTabName;

    }

    /**
     * Добавляет новую вкладку к инвентарю. Новая вкладка будет иметь размер, равный {@link #tabSlotsCount}.
     * @param tabName Название новой вкладки
     */
    public void addTab(String tabName) {

        // debug
        Tab t = new Tab();
        if (items.get(tabName) == null) {
            for (int i = 0; i < t.stacks.length; i++) {
                ItemStack ii = new ItemStack(getRandomItem(), 1);
                t.stacks[i] = ii;
            }
        }

        items.put(tabName, t);
        if (currentTabKey == null) {
            currentTabKey = tabName;
        }
    }

    // debug
    public static Item getRandomItem() {
        Item i = null;
        Object[] objects = Item.itemRegistry.getKeys().toArray();
        Random r = new Random();

        do {
            //Object select = objects[r.nextInt(objects.length)];
            i = Item.getItemById(r.nextInt(objects.length));
        } while (i == null);

        return i;
    }

    /**
     * Добавляет новую вкладку к инвентарю. Новая вкладка будет иметь размер, равный {@link #tabSlotsCount}.
     * @param tabName Название новой вкладки
     * @param stackLimit Лимит предметов в стаках вкладки
     */
    public void addTab(String tabName, int stackLimit) {
        items.put(tabName, new Tab(stackLimit));
    }

    private void addTab(String tabName, ItemStack[] content) {
        Tab t = new Tab();
        t.stacks = content;
        items.put(tabName, t);
    }



    /**
     * Удаляет вкладку с указаннм названием, вместе со всем ее содержимым.
     * @param tabName Название вкладки
     */
    public void removeTab(String tabName) {
        items.remove(tabName);
        if (items.isEmpty()) {
            currentTabKey = null;
        }
    }











    // TODO
    private class Tab {
        ItemStack[] stacks;
        final int stackLimit;

        /**
         * Конструктор для создания дефолтной вкладки
         */
        public Tab() {
            this.stacks = new ItemStack[tabSlotsCount];
            this.stackLimit = 64;
        }

        /**
         * Конструктор, для создания вкладки с заданным лимитом для стаков
         * @param stackLimit
         */
        public Tab(int stackLimit) {
            this.stacks = new ItemStack[tabSlotsCount];
            this.stackLimit = stackLimit;
        }

        public boolean isItemValidForSlotInTab(int slotIndex, ItemStack itemStack) {
            return true; // TODO
        }
    }




    /*public static class MessageHandler implements IMessageHandler<TabHostInventory.SetCurrentTabPacket, IMessage> {

        public MessageHandler() {
        }

        @Override
        public IMessage onMessage(TabHostInventory.SetCurrentTabPacket message, MessageContext ctx) {
            ExtendedPlayer.get(ctx.getServerHandler().playerEntity).tabInventory.currentTabKey = message.newCurrentTabName;
            return null;
        }
    }*/
}


