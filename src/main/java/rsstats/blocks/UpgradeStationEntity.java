package rsstats.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import rsstats.inventory.UpgradeStationInventory;

/**
 * Сущность блока верстака улучшения. Инкапсулирует в себе инвентарь сущности и отвечает за
 * запись/чтение в NBT.
 */
public class UpgradeStationEntity extends TileEntity {
    /** Инвентарь блока */
    public UpgradeStationInventory upgradeStationInventory = new UpgradeStationInventory();

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

    // http://cazzar.net/tutorials/minecraft/Tile-Entity-Updates-The-Quick-and-Dirty-Method/
    public void markForUpdate() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
