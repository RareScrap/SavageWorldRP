package rsstats.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.items.StatItems;
import ru.rarescrap.tabinventory.TabHostInventory;

/**
 * Инвентарь для статов игрока (сила, ловкость, выносливость и т.д.)
 * @author RareScrap
 */
public class StatsInventory extends TabHostInventory {
    /* This is also the place to define which slot is which if you have different types,
     * for example SLOT_SHIELD = 0, SLOT_AMULET = 1; */

    /**
     * Конструктор
     * @param inventoryName Имя инвентаря-хоста вкладок
     * @param inventorySize Размер инвентаря-хоста
     */
    public StatsInventory(String inventoryName, int inventorySize) {
        super(inventoryName, inventorySize);
    }
    
    /*
    // Было в туториале но зачем - хз
    @Override
    public boolean isInvNameLocalized() {
        return name.length() > 0;
    }*/

    /**
     * @return Масимальный размер стака для предметов в этом инвенторе
     */
    @Override
    public int getInventoryStackLimit() {
        return 1;
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
     * Проверяет, можно ли поместить предмет в данный слот инвентаря
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
        compound.setTag(getInventoryName(), items);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(getInventoryName(), Constants.NBT.TAG_COMPOUND);

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
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }
    }

    /**
     * Инициализирует начальные статы
     */
    public void initItems() {
        Object[] stats = StatItems.getAll().values().toArray();
        for (int i = 0; i < stats.length; i++) {
            setInventorySlotContents(i, new ItemStack((StatItem) stats[i]));
        }

        markDirty();
    }

    // TODO: В утилиты
    /**
     * Получаем первый попавшийся стак из по указанному UnlocalizedName
     * @param unlocalizedSkillName UnlocalizedName нужного стака
     * @return Первый попавшийся полходящий стак. Если ничего не найдено - null.
     */
    public ItemStack getStat(String unlocalizedSkillName) {
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null && stack.getUnlocalizedName().equals(unlocalizedSkillName)) {
                return stack;
            }
        }

        return null;
    }

    /**
     * Очищает инвентарь
     */
    public void totalClear() {
        for (int i = 0; i < getSizeInventory(); ++i) {
            setInventorySlotContents(i, null);
        }
    }

    // TODO: В утилиты
    /**
     * @return Возвращает все содержимое инвентаря в порядке расположения
     */
    public ItemStack[] getStats() {
        ItemStack[] stacks = new ItemStack[getSizeInventory()];
        for (int i = 0; i < getSizeInventory(); i++) {
            stacks[i] = getStackInSlot(i);
        }
        return stacks;
    }
}
