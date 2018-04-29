package rsstats.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import rsstats.items.StatItem;

// TODO: Контейнер не адаптирован под вызов из консоли

/**
 * Контейнер для {@link rsstats.client.gui.UpgradeGUI}
 */
public class UpgradeContainer extends Container {
    private InventoryPlayer inventoryPlayer;
    /** Инвентарь, встроенные в {@link rsstats.inventory.container.rsstats.blocks.UpgradeStationBlock}. Если null, то
     * контейнер вызывается для GUI по приказу консольной команды {@link rsstats.common.command.OpenWindow}.*/
    private IInventory blockInventory;
    /** Инвентарь с одним единственным слотом, хранящий результат крафта */
    private IInventory outputSlot = new InventoryCraftResult();
    /** Инвентарь с двумя слотами, хранящий реаленты крафта */
    private IInventory inputSlots = new InventoryBasic("Upgrade", true, 2) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
         * hasn't changed and skip it.
         */
        @Override
        public void markDirty() {
            super.markDirty();
            // Рекалкулейтим результат, в ответ на изменение слотов реагентов
            UpgradeContainer.this.onCraftMatrixChanged(this);
        }
    };

    private String description = "";

    /**
     * Конструктор, иницилазирующий свои поля
     * @param inventoryPlayer инвентарь игрока
     * @param blockInventory инвентарь блока. Если null - контейнер вызван для GUI, вызванное командой консоли
     */
    public UpgradeContainer(InventoryPlayer inventoryPlayer, IInventory blockInventory) {
        this.inventoryPlayer = inventoryPlayer;
        this.blockInventory = blockInventory;
        addSlots();
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return true;
    }

    private void addSlots() {
        // Добавляем слот реагентов
        this.addSlotToContainer(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlotToContainer(new Slot(this.inputSlots, 1, 76, 47) {
            @Override
            public boolean isItemValid(ItemStack itemStack) {
                // Во второй слот можно затолкать только стату/навык, для которого создается улучшение
                return itemStack.getItem() instanceof StatItem;
            }
        });

        // Добавляем слот результата крафта
        this.addSlotToContainer(new Slot(this.outputSlot, 2, 134, 47) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            // Отключаем возможность что-то класть в слот результата крафта
            @Override
            public boolean isItemValid(ItemStack p_75214_1_) {
                return false;
            }

            // TODO: Проверка на наличие материалов и скиллов для крафта
            @Override
            public boolean canTakeStack(EntityPlayer p_82869_1_) {
                return super.canTakeStack(p_82869_1_);
            }

            // TODO: роллить скилл и удалять реагенты для крафта
            @Override
            public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack p_82870_2_) {
                super.onPickupFromSlot(p_82870_1_, p_82870_2_);
            }
        });


        // Расставляем слот инвентаря блока
        if (isCalledFromBlock()) { // Расставляем слоты для инвентаря блока только в том случае, если вызывается GUI лоя блока
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlotToContainer(new Slot(blockInventory, x + y * 9, (x * 18 + 0) + 8, (y * 18) + 84));
                }
            }
        }

        int handY = (isCalledFromBlock()) ? 200 : 142;
        // Расставляем слоты на панели руки
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 +0) +8, handY));
        }

        int playerInvOffsetY = (isCalledFromBlock()) ? 142 : 84;
        // Расставляем слот инвенторя игрока
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(inventoryPlayer, x + (y + 1) * 9, (x*18 +0) +8, (y * 18) + playerInvOffsetY));
            }
        }

    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        super.addCraftingToCrafters(p_75132_1_);
        this.detectAndSendChanges(); // TODO: ХЗ зачем это. Наверное, взял из кода ванилы
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory == this.inputSlots) { // Если изменилась входная матрица - перерасчитваем итоговый предмет
            this.updateUpgradeOutput();
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        super.onContainerClosed(entityPlayer);

        // TODO: Что делать с реагентами, когда инвентарь закрыт

        // Тупо вбрасваем как в верстаке
        /*if (!this.theWorld.isRemote)
        {
            for (int i = 0; i < this.inputSlots.getSizeInventory(); ++i)
            {
                ItemStack itemstack = this.inputSlots.getStackInSlotOnClosing(i);

                if (itemstack != null)
                {
                    entityPlayer.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }*/
    }

    // TODO: рекалкулейтить выходной предмет
    public void updateUpgradeOutput() {
        ItemStack itemstack = this.inputSlots.getStackInSlot(0);

        if (itemstack == null || inputSlots.getStackInSlot(1) == null) {
            // Реагент не подходят - результата крафта нет
            this.outputSlot.setInventorySlotContents(0, null);
        } else { // Реагент подошли, вычисляем результат крафта
            ItemStack resultItemStack = itemstack.copy(); // Копируем исходный предмет

            // Получаем его NBT
            NBTTagCompound data = resultItemStack.stackTagCompound;
            if (data == null) {
                data = new NBTTagCompound();
            }

            // Пробуем получить из данных уже имеющиеся модификаторы
            // Проверять на null не нужно, т.к. метод возвратит пустой список, если не найдет его
            NBTTagList modifiers = data.getTagList("modifiers", Constants.NBT.TAG_COMPOUND);

            // Создаем NBT запись модификатора
            NBTTagCompound modifier = new NBTTagCompound();
            modifier.setString("description", description);
            modifier.setString("to", inputSlots.getStackInSlot(1).getItem().getUnlocalizedName());
            modifier.setInteger("value", 5);

            // Добавляем модификатор к другим модификаторам
            modifiers.appendTag(modifier);

            // Сохраняем модификаторы
            data.setTag("modifiers", modifiers);

            // Достаем данные в тултипе
            NBTTagCompound display = data.getCompoundTag("display");
            if (display == null) {
                display = new NBTTagCompound();
            }
            NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_COMPOUND);

            // И добавляем к тултипу текстовое представление модификатора
            lore.appendTag(new NBTTagString("+5 " + description)); // TODO: Не добавляется представление модификаторов, если добавлен второй модификатор

            // Сохраняем тултип
            display.setTag("Lore", lore);
            data.setTag("display", display);

            // Сохраняем вчисления в итоговый предмет
            resultItemStack.stackTagCompound = data;
            this.outputSlot.setInventorySlotContents(0, resultItemStack);

            // Эта хуйня делает так, чтоб надпись описания улучшения мигала на выходном предмете. Связано с разными данными на сторонах
            //x();

            this.detectAndSendChanges(); // TODO: Хз зачем это
        }
    }

    public void updateModifierDescription(String description) {
        this.description = description;
    }

    /*@SideOnly(Side.CLIENT)
    private void x() {
        CommonProxy.INSTANCE.sendToServer(new PacketSyncGUI(description, 5));
    }*/


    public boolean isCalledFromBlock() {
        return blockInventory != null;
    }

    public boolean isTextFieldAvailable() {
        return inputSlots.getStackInSlot(0) != null;
    }
}
