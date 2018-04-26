package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import rsstats.inventory.UpgradeStationInventory;

/**
 * Сущность блока верстака улучшения. Инкапсулирует в себе инвентарь сущности и отвечает за
 * запись/чтение в NBT.
 */
public class UpgradeStationEntity extends TileEntity {
    /** Инвентарь блока */
    public UpgradeStationInventory upgradeStationInventory;

    public UpgradeStationEntity() {
        super();
        this.upgradeStationInventory = new UpgradeStationInventory();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        upgradeStationInventory.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        upgradeStationInventory.readFromNBT(compound);
    }
}
