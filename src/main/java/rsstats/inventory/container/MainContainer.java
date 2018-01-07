package rsstats.inventory.container;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.inventory.slots.SkillSlot;
import rsstats.inventory.slots.StatSlot;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rares
 */
public class MainContainer extends Container {
    private final EntityPlayer player;
    private final InventoryPlayer inventoryPlayer;
    private final StatsInventory statsInventory;
    private final WearableInventory wearableInventory;

    private final SkillsInventory skillsInventory;

    public MainContainer(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory, SkillsInventory skillsInventory, WearableInventory wearableInventory) {
        this.player = player;
        this.inventoryPlayer = inventoryPlayer;
        this.statsInventory = statsInventory;
        this.skillsInventory = skillsInventory;
        this.wearableInventory = wearableInventory;
        addSlots();
    }


    public MainContainer() {
        this.player = null;
        this.inventoryPlayer = null;
        this.statsInventory = null;
        this.skillsInventory = null;
        this.wearableInventory = null;
    }
    
    private void addSlots() {
        /*if (inventoryPlayer != null)
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }*/

        // Расставляем слоты на панели руки
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 -3) +8, 188));
        }

        // Расставляем слоты на панели статов
        for (int i = 0, slotIndex = 0; i < statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(statsInventory, i, (i*18 +167) +8, /*-24*/8));
            //this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Расставляем слоты на панели скиллов
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new SkillSlot(skillsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 26));
            }
        }

        // Расставляем слоты для брони
        for (int i = 0; i < 4; ++i)
        {
            final int k = i;
            this.addSlotToContainer(new Slot(inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i, (i*18 + 51) +8, 8)
            {

                @Override
                public int getSlotStackLimit() { return 1; }
                @Override
                public boolean isItemValid(ItemStack par1ItemStack)
                {
                    if (par1ItemStack == null) return false;
                    return par1ItemStack.getItem().isValidArmor(par1ItemStack, k, player);
                }
                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex() {
                    return ItemArmor.func_94602_b(k);
                }



                @Override
                public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack itemStack) {
                    super.onPickupFromSlot(p_82870_1_, itemStack);
                    ExtendedPlayer.get(player).removeModifiersFromItemStack(itemStack); // Удаляем модификаторы от прошлой брони
                }

                /**
                 * Helper method to put a stack in the slot.
                 *
                 * @param itemStack
                 */
                @Override
                public void putStack(ItemStack itemStack) {
                    if (!player.worldObj.isRemote) {
                        // Если в слоте уже был предмет - удаляем его модификаторы
                        if (this.getStack() != null) {
                            ExtendedPlayer.get(player).removeModifiersFromItemStack(this.getStack());
                        }
                        // Извлекаем и сохраняем модификаторы из стака, который кладется в слот
                        ExtendedPlayer.get(player).extractModifiersFromItemStack(itemStack);
                    }
                    super.putStack(itemStack);
                }
            });
        }

        // Расставляем слоты на панели носимых вещей
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                this.addSlotToContainer(new Slot(wearableInventory, x + y * 4 /*+ 9*/, (x*18 + 51) +8, (y * 18) + 26) {
                    // Сюда нельзя помещать броню
                    @Override
                    public boolean isItemValid(ItemStack p_75214_1_) {
                        if (p_75214_1_.getItem() instanceof ItemArmor)
                            return false;
                        else
                            return super.isItemValid(p_75214_1_);
                    }
                });
            }
        }
    }

    /**
     * This should always return true, since custom inventory can be accessed from anywhere
     * @param player TODO
     * @return TODO
     */
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
    
    /**
     * Called when a entityPlayer shift-clicks on a slot. You must override this or you will crash when someone does that.
     * Basically the same as every other container I make, since I define the same constant indices for all of them
     * @param player TODO
     * @param par2 TODO
     * @return TODO
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(par2);
        return itemstack;
    }

    // TODO: баг при стаках очков прокачки 64 и 1
    /**
     * Увеличивает переданную стату на 1, если в {@link #inventoryPlayer} есть хотя бы один {@link rsstats.items.ExpItem}.
     * Так же уменьшает ExpItem на 1.
     * @param slot Слот0 в котором находится стата, которую необходимо прокачать
     */
    private void statUp(Slot slot) {
        StatItem statItem = (StatItem) slot.getStack().getItem();

        // Находим стак с очками прокачки
        ItemStack expStack = null;
        for (ItemStack itemStack : inventoryPlayer.mainInventory) {
            if (itemStack != null && "item.ExpItem".equals(itemStack.getUnlocalizedName())) {
                expStack = itemStack;
            }
        }
        if (expStack == null) {
            return; // Если очков прокачки нет - выходим
        }

        // Выявляет количество подтипов (уровней) статы
        List subitems = new ArrayList();
        statItem.getSubItems(statItem, CreativeTabs.tabMaterials, subitems);

        int statItemDamage = statItem.getDamage(slot.getStack());
        if (statItemDamage != subitems.size() - 1) {
            int price = 1; // Цена прокачки
            if (statItem instanceof SkillItem) {
                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
                int parentStatDamage = ((SkillItem) statItem).parentStat.getDamage(parentStatStack);
                if (statItemDamage > parentStatDamage)
                    price = 2;
            }

            // Отнимает очко прокачки ...
            if (expStack.stackSize >= price) {
                expStack.stackSize -= price;
            } else {
                return;
            }

            // и увеличиваем стату ...
            statItem.setDamage(
                    slot.getStack(),
                    statItemDamage < subitems.size() - 1 ? statItemDamage + 1 : subitems.size() - 1
            );
        } else { // Стата уже прокачана до предела - выходим
            return;
        }
    }

    private void statDown(Slot slot) {
        StatItem statItem = (StatItem) slot.getStack().getItem();

        // Находим стак с очками прокачки
        ItemStack expStack = null;
        for (ItemStack itemStack : inventoryPlayer.mainInventory) {
            if (itemStack != null && "item.ExpItem".equals(itemStack.getUnlocalizedName())) {
                expStack = itemStack;
            }
        }
        // Если очков прокачки нет или их стак забит - ищем свободное место в инветаре, куда их можно положить
        if (expStack == null || expStack.stackSize >= expStack.getMaxStackSize()) {
            int freeSpaceIndex = findFreeSpaceInInventory(inventoryPlayer);
            if (freeSpaceIndex != -1) {
                expStack = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"));
                inventoryPlayer.setInventorySlotContents(freeSpaceIndex, expStack);
            } else { // Если свободное место не было найдено - выходим
                return;
            }
        }

        // Выявляет количество подтипов (уровней) статы
        List subitems = new ArrayList();
        statItem.getSubItems(statItem, CreativeTabs.tabMaterials, subitems);

        int statItemDamage = statItem.getDamage(slot.getStack());
        if (statItemDamage > 0) {
            int reward = 0; // Сколько очков прокачки вернется за отмену прокачки
            if (statItem instanceof SkillItem) {
                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
                int parentStatDamage = ((SkillItem) statItem).parentStat.getDamage(parentStatStack);
                if (statItemDamage > parentStatDamage+1)
                    reward = 2;
                else
                    reward = 1;
            } else { // instanceof StatItem
                reward = 1;
            }

            // возвращаем игроку очки прокачки ...
            if (expStack.stackSize + reward <= expStack.getMaxStackSize()) {
                expStack.stackSize += reward;
            } else {
                reward = (expStack.stackSize + reward) % expStack.getMaxStackSize();
                expStack.stackSize = expStack.getMaxStackSize();

                int freeSpaceIndex = findFreeSpaceInInventory(inventoryPlayer);
                if (freeSpaceIndex != -1) {
                    expStack = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"));
                    expStack.stackSize = reward;
                    inventoryPlayer.setInventorySlotContents(freeSpaceIndex, expStack);
                } else {
                    return;
                }
            }

            // и уменьшаем стату ...
            statItem.setDamage(
                    slot.getStack(),
                    statItemDamage > 0 ? statItemDamage-1 : 0
            );
        } else { // Стата уже прокачана до предела - выходим
            return;
        }
    }

    /**
     * Находит и возвращает тот порядковы номер слота, который не содержит в себе {@link ItemStack}'а
     * @param inventory Ивентарь, в котором производится поиск
     * @return Индекс свободной ячейки инвентаря. Если свободных ичеет нет, метод вернет -1.
     */
    private int findFreeSpaceInInventory(IInventory inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        Slot slot;
        try {
            slot = getSlot(slotId);
        } catch(Exception e) {
            return super.slotClick(slotId, clickedButton, mode, playerIn);
            //return null; // костыль
        }
        Item itemInSlot;
        if (slot.getStack() != null && slot.getStack().getItem() != null) {
            itemInSlot = slot.getStack().getItem();
        } else {
            return super.slotClick(slotId, clickedButton, mode, playerIn);
            //return null;
        }

        if (clickedButton == 1) { // ПКМ
            statUp(slot);
            return null;
        }
        if (clickedButton == 2) { // СКМ
            statDown(slot);
            return null;
        }

        if ((slot.inventory == statsInventory || slot.inventory == skillsInventory) && (itemInSlot instanceof SkillItem || itemInSlot instanceof StatItem)) {
            ItemStack itemStack = getSlot(slotId).getStack();

            // Защита от дублирующихся сообщений в чате
            if (!playerIn.worldObj.isRemote) {
                ( (StatItem) itemStack.getItem() ).roll(itemStack, playerIn);
            }
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    public SkillsInventory getSkillsInventory() {
        return skillsInventory;
    }
}