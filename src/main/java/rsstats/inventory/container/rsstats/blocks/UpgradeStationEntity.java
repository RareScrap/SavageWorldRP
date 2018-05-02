package rsstats.inventory.container.rsstats.blocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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

    // TODO: Сохранять направление в NBT - нерационально. Нужно юзать метадату - http://www.minecraftforge.net/forum/topic/25239-solved1710-saving-block-direction-state/
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        upgradeStationInventory.writeToNBT(compound);
        compound.setInteger("dir", this.blockMetadata);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        upgradeStationInventory.readFromNBT(compound);
        this.blockMetadata = compound.getInteger("dir");

    }

    // Синхронизирует переменные блока с блоком на клиенте
    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    // Нужно для метода выше
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    // http://cazzar.net/tutorials/minecraft/Tile-Entity-Updates-The-Quick-and-Dirty-Method/
    public void markForUpdate() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
