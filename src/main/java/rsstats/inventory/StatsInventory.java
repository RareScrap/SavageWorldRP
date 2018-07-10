package rsstats.inventory;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

/**
 * Инвентарь для статов игрока (сила, ловкость, выносливость и т.д.)
 * @author RareScrap
 */
public class StatsInventory implements IInventory {
    /** The key used to store and retrieve the inventory from NBT */
    private static final String NBT_TAG = "stats";

    /** Define the inventory size here for easy reference */
    /* This is also the place to define which slot is which if you have different types,
     * for example SLOT_SHIELD = 0, SLOT_AMULET = 1; */
    public static final int INV_SIZE = 9;
    /** Масимальный размер стака для предметов в инвенторе {@link #inventory} */
    private static final int STACK_LIMIT = 1;

    /** Структура, хранящая предметы инвентаря в стаках.
     * Inventory's size must be same as number of slots you add to the Container class. */
    private ItemStack[] inventory = new ItemStack[INV_SIZE];
    /** Игрок, к которому привязан инвентарь */
    private EntityPlayer entityPlayer;

    /**
     * Необходимый публичный контсруктор
     */
    public StatsInventory(EntityPlayer entityPlayer) {
        this.entityPlayer = entityPlayer;
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
     * @return Стак предметов под индексом slotIndex в инвенторе
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
     * Clears a slot and returns it's previous content. Аналог removeStackFromSlot() в более новых версиях.
     * @param slotIndex
     * @return
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        ItemStack stack = getStackInSlot(slotIndex);
        setInventorySlotContents(slotIndex, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        this.inventory[slotIndex] = itemStack;
        if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }
        markDirty();
    }

    /**
     * Возвращает локализованное имя инвентаря
     * @return Имя инвентаря
     */
    @Override
    public String getInventoryName() {
        return StatCollector.translateToLocal("inventory." + NBT_TAG);
    }
    
    /*
    // Было в туториале но зачем - хз
    @Override
    public boolean isInvNameLocalized() {
        return name.length() > 0;
    }*/

    @Override
    public boolean hasCustomInventoryName() {
        return false;
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
        // ТОDO: ХЗ что это мы делаем. Удаляем мусор? Гарантированно очищаем слоты?
        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i) != null && getStackInSlot(i).stackSize == 0) {
                inventory[i] = null;
            }
        }

        // TODO: Проверить при помощи NBTEdit как мод ведет себя без этой строки
        writeToNBT(entityPlayer.getEntityData());
    }

    /**
     * Returns true if the given player has access to the inventory. The default implementation just checks
     * the player's distance to the TileEntity and returns true if it is less than 8 blocks. The method uses
     * the player's method getDistanceSq which returns the squared distance to the given point. If this is
     * less than 64, the real distance is less than 8.
     *
     * Инвентарь может использоваться игроком?
     * @param entityPlayer Сущность игрока, взаимодействующая с инвентарем
     * @return false
     */
    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        // TODO: Без понятия как это работает и зачем нужно
        return entityPlayer.capabilities.isCreativeMode;
        // return true;
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

        // TODO: Хочу использовать уже реализованную проверку в StatSlot, но не нзнаю как
        return itemStack.getItem() instanceof StatItem && !(itemStack.getItem() instanceof SkillItem);
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

        /* Если инвентарь статов пустой или не содержвится в пришедшем compound'е (а он скорее всего содержится, см init())
         * - добавляем стандартный набор статов */
        if (items.tagCount() == 0) {
            initItems();
            return;
        }

        // Штатное чтение из NBT
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }

    /**
     * Инициализирует начальные статы
     */
    public void initItems() {
        for (int i = 0; i < CommonProxy.Stats.values().length; i++) {
            inventory[i] = new ItemStack(GameRegistry.findItem(RSStats.MODID, CommonProxy.Stats.values()[i].toString()));
        }

        markDirty();
    }

    /**
     * Получаем стак из {@link #inventory} по указанному UnlocalizedName
     * @param unlocalizedSkillName UnlocalizedName нужного стака скилла
     * @return Стак {@link StatItem}'а
     */
    public ItemStack getStat(String unlocalizedSkillName) {
        for (ItemStack stat : inventory) {
            if (stat.getUnlocalizedName().equals(unlocalizedSkillName)) {
                return stat;
            }
        }
        return null;
    }

    /**
     * Очищает все имеющиеся хранилища {@link ItemStack}'ов
     */
    public void totalClear() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            inventory[i] = null;
        }
    }

    /**
     * Устанавливает новое содержимое для хранилища статов ({@link #inventory})
     * @param stats Обновленное хранилие статов
     */
    public void setNewStats(ItemStack[] stats) {
        this.inventory = stats;

        // Если выведено GUI - обновим его на всякий случай
        if (Minecraft.getMinecraft().currentScreen != null) {
            Minecraft.getMinecraft().currentScreen.updateScreen();
        }
    }

    // TODO: Заменить на getAll и преопределять в потомках
    /**
     * Геттер для {@link #inventory}.
     * @return Массив стаков, представляющих собой инвентарь статов
     */
    public ItemStack[] getStats() {
        return inventory;
    }
}
