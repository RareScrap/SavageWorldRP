package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;

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
}
