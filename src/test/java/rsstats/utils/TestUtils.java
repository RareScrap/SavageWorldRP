package rsstats.utils;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@PrepareForTest({Item.class, Block.class, Blocks.class})
public class TestUtils {
    Random r = new Random();

    @Test
    public void TestRemoveItemStackFromInventory() {
        InventoryBasic inventoryBasic = new InventoryBasic("Test", false, r.nextInt(1000));

        // Попытка удаления при пустом инвентаре
        Item itemDirt = new Item().setUnlocalizedName("item.DummyBlock");
        boolean result = Utils.removeItemStackFromInventory(inventoryBasic,
                itemDirt.getUnlocalizedName(),
                1);
        assertEquals(result, false);



        // Попытка удалить несуществующий предмет
        for (int i = 0; i < inventoryBasic.getSizeInventory(); i++) { // Заполняем рандомными предметами
            if (r.nextInt(2) % 2 == 0) { // шанс заполнить слот - 50/50
                Item randomItem = new Item().setUnlocalizedName("item.randomItem" + i).setMaxStackSize(1 + r.nextInt(64));
                ItemStack itemStack = new ItemStack(randomItem, 1 + r.nextInt(randomItem.getItemStackLimit())); // TODO: Depricated
                inventoryBasic.setInventorySlotContents(i, itemStack);
            }
        }
        result = Utils.removeItemStackFromInventory(inventoryBasic, "item.nonexistent", 1);
        assertEquals(false, result);



        // Штатная работа №1
        inventoryBasic = new InventoryBasic("Test", false, 27);
        ArrayList<Item> dummyItems = new ArrayList<Item>();
        for (int i = 0; i < 7; i++) {
            dummyItems.add(new Item().setUnlocalizedName("dummyItem" + i));
        }
        for (int i = 0; i < inventoryBasic.getSizeInventory()-1; i++) {
            Item dummyItem = dummyItems.get( r.nextInt(dummyItems.size()) );
            int stackSize = 1 + r.nextInt(dummyItem.getItemStackLimit()); // TODO: Depricated
            inventoryBasic.setInventorySlotContents(i, new ItemStack(dummyItem, stackSize));
        }
        ItemStack gurantedSearchResult = new ItemStack(dummyItems.get(0), 20);
        inventoryBasic.setInventorySlotContents(inventoryBasic.getSizeInventory()-1, gurantedSearchResult);
        result = Utils.removeItemStackFromInventory(inventoryBasic,
                dummyItems.get(0).getUnlocalizedName(),
                1 + r.nextInt(20));
        assertEquals(true, result);



        // Все стаки кроме одного подлежат удалению
        inventoryBasic = new InventoryBasic("Test", false, 27);
        for (int i = 0; i < inventoryBasic.getSizeInventory(); i++) {
            inventoryBasic.setInventorySlotContents(i, new ItemStack(new Item().setUnlocalizedName("dummyItem"), 64));
        }
        inventoryBasic.setInventorySlotContents(r.nextInt(27), new ItemStack(new Item().setUnlocalizedName("notDummyItem")));
        result = Utils.removeItemStackFromInventory(inventoryBasic, "item.dummyItem", 1664);
        int stackCount = 0;
        for (int i = 0; i < inventoryBasic.getSizeInventory(); i++) {
            ItemStack itemStack = inventoryBasic.getStackInSlot(i);
            if (itemStack != null) stackCount++;
        }
        assertEquals(true, result & (stackCount == 1));
    }
}
