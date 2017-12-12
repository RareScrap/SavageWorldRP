package rsstats.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.slots.StatSlot;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

/**
 *
 * @author rares
 */
public class MainContainer extends Container {
    private final EntityPlayer player;
    private final InventoryPlayer inventoryPlayer;
    private final StatsInventory statsInventory;
    private final SkillsInventory skillsInventory;
   
    public MainContainer(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory, SkillsInventory skillsInventory) {
        this.player = player;
        this.inventoryPlayer = inventoryPlayer;
        this.statsInventory = statsInventory;
        this.skillsInventory = skillsInventory;
        addSlots();
    }


    public MainContainer() {
        this.player = null;
        this.inventoryPlayer = null;
        this.statsInventory = null;
        this.skillsInventory = null;
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
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 -87) +8, 136));
        }

        // Расставляем слоты на панели статов
        for (int i = 0, slotIndex = 0; i < statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(statsInventory, i, (i*18 +83) +8, -44));
            //this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Расставляем слоты на панели скиллов
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(skillsInventory, x + y * 9 /*+ 9*/, (x*18 +83) +8, (y * 18) - 26));
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

        if ((slot.inventory == statsInventory || slot.inventory == skillsInventory) && (itemInSlot instanceof SkillItem || itemInSlot instanceof StatItem)) {
            ItemStack itemStack = getSlot(slotId).getStack();
            ( (StatItem) itemStack.getItem() ).roll(itemStack, playerIn);
            return null;
        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }



}
