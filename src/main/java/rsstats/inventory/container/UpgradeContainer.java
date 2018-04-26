package rsstats.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * Контейнер для {@link rsstats.client.gui.UpgradeGUI}
 */
public class UpgradeContainer extends Container {
    private InventoryPlayer inventoryPlayer;
    /** Инвентарь, встроенные в {@link rsstats.inventory.container.rsstats.blocks.UpgradeStationBlock}. Если null, то
     * контейнер вызывается для GUI по приказу консольной команды {@link rsstats.common.command.OpenWindow}.*/
    private IInventory blockInventory;

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

    public boolean isCalledFromBlock() {
        return blockInventory != null;
    }
}
