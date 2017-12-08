package rsstats.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import rsstats.inventory.slots.StatSlot;

/**
 *
 * @author rares
 */
public class MainContainer extends Container {
    private final EntityPlayer player;
    private final InventoryPlayer inventoryPlayer;
   
    public MainContainer(EntityPlayer player, InventoryPlayer inventoryPlayer) {
        this.player = player;
        this.inventoryPlayer = inventoryPlayer;
        addSlots();
    }

    public MainContainer() {
        this.player = null;
        this.inventoryPlayer = null;
    }
    
    private void addSlots() {
        /*if (inventoryPlayer != null)
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }*/
        
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 -87) +8, 136));
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
        return itemstack;
    }
}
