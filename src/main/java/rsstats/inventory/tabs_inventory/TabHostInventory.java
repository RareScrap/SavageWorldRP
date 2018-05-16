package rsstats.inventory.tabs_inventory;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;

/**
 * Инвентарь, основная цель которого - устанавливать отображение
 */
public class TabHostInventory extends InventoryBasic {
    private static boolean isHandlerRegistered = false;
    private static final String NBT_TAG = "player_data_tabs";
    private String currentTab;
    private TabInventory tabInventory;

    public TabHostInventory(boolean hasCustomInventoryName, int inventorySize, TabInventory tabInventory) {
        super(NBT_TAG, hasCustomInventoryName, inventorySize);
        this.tabInventory = tabInventory;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void setInventorySlotContents(int p_70299_1_, ItemStack p_70299_2_) {
        super.setInventorySlotContents(p_70299_1_, p_70299_2_);
        if (p_70299_2_ != null) {
            tabInventory.addTab(p_70299_2_.getUnlocalizedName());
        }
    }

    public boolean isEmpty() {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
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
        /*if (items.tagCount() == 0) {
            initItems();
            return;
        }*/

        // Штатное чтение из NBT
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }
    }





    public void setTabInventory(TabInventory tabInventory) {
        this.tabInventory = tabInventory;
    }

    public static void registerHandler(Class<? extends IMessageHandler<SetCurrentTabPacket, IMessage>> handler, int discriminator) {
        if (isHandlerRegistered) {
            throw new RuntimeException("Handler is already registered!");
        } else {
            //handler = TabMessageHandler.class;
            CommonProxy.INSTANCE.registerMessage(handler, SetCurrentTabPacket.class, discriminator, Side.SERVER);
            isHandlerRegistered = true;
        }
    }

    public void setTab(String tabName) {
        if (isHandlerRegistered) {
            if (!tabName.equals(currentTab)) {
                CommonProxy.INSTANCE.sendToServer(new SetCurrentTabPacket(tabInventory.getInventoryName(), tabName));
                currentTab = tabName;
            }
        } else {
            throw new RuntimeException("Handler is not registered.");
        }
    }

    public static class SetCurrentTabPacket implements IMessage {
        String inventoryName;
        String newCurrentTabName;

        public SetCurrentTabPacket() {
        }

        public SetCurrentTabPacket(String inventoryName, String newCurrentTabName) {
            this.inventoryName = inventoryName;
            this.newCurrentTabName = newCurrentTabName;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            inventoryName = ByteBufUtils.readUTF8String(buf);
            newCurrentTabName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, inventoryName);
            ByteBufUtils.writeUTF8String(buf, newCurrentTabName);
        }
    }
}
