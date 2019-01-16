package rsstats.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import rsstats.blocks.Blocks;
import rsstats.blocks.UpgradeStationBlock;
import rsstats.blocks.UpgradeStationEntity;
import rsstats.blocks.UpgradeStationTESR;
import rsstats.client.gui.MainMenuGUI;
import rsstats.client.gui.SSPPage;
import rsstats.client.gui.UpgradeGUI;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.event.KeyHandler;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.UpgradeContainer;

/**
 * Прокси, исполняемый на стороне клиента
 * @author RareScrap
 */
public class ClientProxy extends CommonProxy {
    /**
     * Получает GUI для указанного ID
     * @param ID идентификатор GUI, объект которого необходимо возвратить
     * @param player Сущность игрока, вызывающего GUI
     * @param world Мир
     * @param x Местоположение сущности игрока по оси X
     * @param y Местоположение сущности игрока по оси Y
     * @param z Местоположение сущности игрока по оси Z
     * @return Потомок класса GuiContainer, соответствующий указанному ID
     */
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        // Проверяем, точно ли мы на клиете
        // TODO: Не могу однозначно сказать что эта проверка делает
        if (world instanceof WorldClient) {
            // Ищем GUI, соответствующий данному ID
            switch (ID) {
                case RSStats.GUI: {
                    return new MainMenuGUI(
                            ExtendedPlayer.get(player),
                            ExtendedPlayer.get(player).mainContainer
                    );
                }
                /*
                TODO: ВНИМАНИЕ! По туториалу, мне не нужно делать проверку в строке 25.
                Более того, мне нужно свитч затолкать в CommonProxy. Но так как
                у меня все работает и при таком раскладе, я пока оставлю все как есть
                */
                case RSStats.SSP_UI_CODE: return new SSPPage(player, player.inventory, ExtendedPlayer.get(player).statsInventory);
                case RSStats.UPGRADE_UI_FROM_BLOCK_CODE: return new UpgradeGUI(new UpgradeContainer(player.inventory, ((UpgradeStationEntity)world.getTileEntity(x, y, z)).upgradeStationInventory));
                case RSStats.UPGRADE_UI_FROM_CMD_CODE: return new UpgradeGUI(new UpgradeContainer(player.inventory, null));
                //case RSStats.DIALOG_GUI_CODE: return new MainMenuGUI.Dialog();
                /* Тут не выйдет открывать диалоговое окно, потому что в один момент времени может быть открыт только
                 * один GuiScreen (см код откртия GUI). Выход - вызывать drawScreen() диалогового окна прямо из того
                 * GuiScreen, над которым нужно отобразить диалог. Используйте этот подход, если вам нужно отобразить
                 * один GuiScreen поверх другого. */
            }
        }
        return null;
    }
    
    // Кей-хандлеры должны регистрироваться только на клиенте
    @Override
    public void registerKeyBindings() {
        keyHandler = new KeyHandler();
        FMLCommonHandler.instance().bus().register(keyHandler);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        //MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(new UpgradeStationBlock()), new UpgradeStationTESR.Renderer(new UpgradeStationTESR(), new UpgradeStationEntity()));
    }

    // TODO: Непонятный пиздец. Хочет найти способ не создавать static поля в CommonProxy, но из-за метода ниже, я не могу этого сделать
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Регистрируем рендереры
        ClientRegistry.bindTileEntitySpecialRenderer(UpgradeStationEntity.class, new UpgradeStationTESR());

        // Пытаюсь как-то получить item для блока

        // Этот способ я использовал чтобы получить item блока, где сам блок не объявлен
        // static переменной при регистрации в CommonProxy. Т.е. я простосоздавал локальную
        // переменную через new UpgradeStationBlock() и регистрировал ее.
        // Результат - любая попытка достать итем блока возвращает null, кроме явного создания нового ItemBlock из блока
        Item d1 = Item.getItemFromBlock(new UpgradeStationBlock()); // null
        ItemBlock d = new ItemBlock(new UpgradeStationBlock());
        Item d12 = GameRegistry.findItem(RSStats.MODID, d.getUnlocalizedName()); // null
        Item d13 = ItemBlock.getItemFromBlock(new UpgradeStationBlock()); // null

        // А тут я делаю то же самое, но теперь использую ту же переменную блока, которая прошла регистрацию в CommonProxy
        // Результат - все вызовы возвращают нормальнйы item
        Item c1 = Item.getItemFromBlock(Blocks.upgradeStation);
        ItemBlock c = new ItemBlock(Blocks.upgradeStation);
        Item c12 = GameRegistry.findItem(RSStats.MODID, Blocks.upgradeStation.getUnlocalizedName());
        Item c13 = ItemBlock.getItemFromBlock(Blocks.upgradeStation);

        // Это не срабатывает, т.к. итем либо null, либо создан явно через консруктор (это, к моему удивлению, не регистрирует ItemRenderer
        //UpgradeStationTESR.Renderer d2 =  new UpgradeStationTESR.Renderer(new UpgradeStationTESR(), new UpgradeStationEntity());
        //MinecraftForgeClient.registerItemRenderer(ItemBlock.getItemFromBlock(new UpgradeStationBlock()), new UpgradeStationTESR.Renderer(new UpgradeStationTESR(), new UpgradeStationEntity()));

        // Только так так ItemRenderer успешно регистрируется и UpgradeStationTESR.Renderer#renderItem() успешно вызывается.
        MinecraftForgeClient.registerItemRenderer(c1, new UpgradeStationTESR.Renderer(new UpgradeStationTESR(), new UpgradeStationEntity()));

        // Как еще можно получить доступ к ItemRenderer'у. Где-то прочитал но зачем это надо - хз
        //Minecraft.getMinecraft().entityRenderer.itemRenderer.
    }
}
