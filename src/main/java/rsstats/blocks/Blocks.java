package rsstats.blocks;

import cpw.mods.fml.common.registry.GameRegistry;

public class Blocks {
    public static final UpgradeStationBlock upgradeStation = new UpgradeStationBlock();

    /* Итемы для блока регистрируюся автоматически. Попытка зарегистрировать их вручную
     * привет к ошибке, т.к. регитсрационный слот под итем уже занят:
     * ОШИБКА: GameRegistry.registerItem(new ItemBlock(upgradeStation), UpgradeStationBlock.item_name); */
    public static void registerBlocks() {
        GameRegistry.registerTileEntity(UpgradeStationEntity.class, "UpgradeStationEntity");
        GameRegistry.registerBlock(upgradeStation, UpgradeStationBlock.name);
    }
}
