package rsstats.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.slots.StatSlot;

/**
 *
 * @author rares
 */
public class StatsContainer extends Container {

    public StatsContainer() {
        System.out.print("StatsContainer()\n");
    }
    
    public StatsContainer(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory) {
        System.out.print("StatsContainer(...)\n");
        //int i;
        
        // Player Inventory, Slot 9-35, Slot IDs 9-35
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new StatSlot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }
        
        for (int i = 0, slotIndex = 0; i < statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Add CUSTOM slots - we'll just add two for now, both of the same type.
        // Make a new Slot class for each different item type you want to add
        /*this.addSlotToContainer(new StatSlot(statsInventory, 0, 80, 8));
        this.addSlotToContainer(new StatSlot(statsInventory, 1, 80, 26));*/

        // Add ARMOR slots; note you need to make a public version of SlotArmor
        // just copy and paste the vanilla code into a new class and change what you need
        /*for (i = 0; i < 10; ++i) {
            this.addSlotToContainer(
                new SlotArmor(player,
                        inventoryPlayer,
                        inventoryPlayer.getSizeInventory() - 1 - i,
                        8,
                        8 + i * 18,
                        i)
            );
        }*/

        // Я ПОКА ЭТО НЕ ТРОГАЮ!
        /*
        // Add vanilla PLAYER INVENTORY - just copied/pasted from vanilla classes
        for (i = 0; i < 3; ++i)
        {
                for (int j = 0; j < 9; ++j)
                {
                        this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
                }
        }

        // Add ACTION BAR - just copied/pasted from vanilla classes
        for (i = 0; i < 9; ++i)
        {
                this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
        */
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
    * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
    * Basically the same as every other container I make, since I define the same constant indices for all of them 
     * @param player TODO
     * @param par2 TODO
     * @return TODO
    */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(par2);
       
        // Я ПОКА НЕ БУДУ ЭТО ТРОГАТЬ!
        /*
        if (slot != null && slot.getHasStack()) {
           ItemStack itemstack1 = slot.getStack();
           itemstack = itemstack1.copy();

           // Either armor slot or custom item slot was clicked
           if (par2 < INV_START) {
               // try to place in player inventory / action bar
               if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END + 1, true)) {
                   return null;
               }

               slot.onSlotChange(itemstack1, itemstack);
           }
           // Item is in inventory / hotbar, try to place either in custom or armor slots
           else {
               // if item is our custom item
               if (itemstack1.getItem() instanceof ItemUseMana) {
                   if (!this.mergeItemStack(itemstack1, 0, InventoryCustomPlayer.INV_SIZE, false)) {
                       return null;
                   }
               }
               // if item is armor
               else if (itemstack1.getItem() instanceof ItemArmor) {
                   int type = ((ItemArmor) itemstack1.getItem()).armorType;
                   if (!this.mergeItemStack(itemstack1, ARMOR_START + type, ARMOR_START + type + 1, false)) {
                           return null;
                   }
               }
               // item in player's inventory, but not in action bar
               else if (par2 >= INV_START && par2 < HOTBAR_START) {
                   // place in action bar
                   if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_START + 1, false)) {
                           return null;
                   }
               }
               // item in action bar - place in player inventory
               else if (par2 >= HOTBAR_START && par2 < HOTBAR_END + 1) {
                   if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
                           return null;
                   }
               }
           }

           if (itemstack1.stackSize == 0) {
               slot.putStack((ItemStack) null);
           } else {
               slot.onSlotChanged();
           }

           if (itemstack1.stackSize == itemstack.stackSize) {
               return null;
           }

           slot.onPickupFromSlot(player, itemstack1);
        }*/

        return itemstack;
    }
    
    
    
    
    
    
    /*
   // Мой старый конструктор
    public StatsContainer(InventoryPlayer inventoryPlayer, TileEntity te) {

        // Player Inventory, Slot 9-35, Slot IDs 9-35
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

    }*/
    
}
