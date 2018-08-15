package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.util.LinkedHashMap;

/**
 * Хранилище разнообразных игровых предметов
 * @author RareScrap
 */
public class MiscItems {
    public static final ExpItem expItem = new ExpItem("ExpItem");
    public static final RerollCoin rerollCoinItem = new RerollCoin("RerollCoinItem");

    public static void registerItems() {
        GameRegistry.registerItem(expItem, "ExpItem");
        GameRegistry.registerItem(rerollCoinItem, "RerollCoinItem");
    }

    /**
     * Возвращает итемы в хранилище формата ИМЯ->ИТЕМ
     */
    public static LinkedHashMap<String, Item> getAll() {
        LinkedHashMap<String, Item> miscItems = new LinkedHashMap<String, Item>();
        miscItems.put("ExpItem", expItem);
        miscItems.put("RerollCoinItem", rerollCoinItem);
        return miscItems;
    }
}
