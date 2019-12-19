package rsstats.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import rsstats.blocks.Blocks;
import rsstats.blocks.UpgradeStationEntity;
import rsstats.common.event.KeyHandler;
import rsstats.common.network.*;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.StatsContainer;
import rsstats.inventory.container.UpgradeContainer;
import rsstats.items.*;
import rsstats.weight.WeightProvider;
import ru.rarescrap.tabinventory.network.NetworkUtils;
import ru.rarescrap.tabinventory.network.TabInventoryItemsMessage;

import static rsstats.common.RSStats.instance;
import static rsstats.common.RSStats.proxy;

/**
 * Проки, содержащий код как для клиента, так и сервера
 * @author RareScrap
 */
public class CommonProxy implements IGuiHandler {
    /** Обработчик нажатия кнопок, используемых для вызова GUI */
    protected KeyHandler keyHandler;
    /** Обертка для работы с сетью */
    public static /*final*/ SimpleNetworkWrapper INSTANCE = // TODO: Филан убран из-за тестов. Восстановить финал
            NetworkRegistry.INSTANCE.newSimpleChannel(RSStats.MODID.toLowerCase());

    public void preInit(FMLPreInitializationEvent event) {
        int discriminator = 0;

        // Когда сообщений станет много, их можно вынести в отдельный класс в метод init()
        INSTANCE.registerMessage(RollPacketToServer.MessageHandler.class, RollPacketToServer.class, discriminator++, Side.SERVER); // Регистрация сообщения о пробросе статы
        INSTANCE.registerMessage(PacketOpenRSStatsInventory.MessageHandler.class, PacketOpenRSStatsInventory.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenSSPPage.MessageHandler.class, PacketOpenSSPPage.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenWindow.MessageHandler.class, PacketOpenWindow.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncGUI.MessageHandler.class, PacketSyncGUI.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketDialogAction.MessageHandler.class, PacketDialogAction.class, discriminator++, Side.SERVER);

        INSTANCE.registerMessage(PacketCommandReponse.MessageHandler.class, PacketCommandReponse.class, discriminator++, Side.CLIENT);

        // Пакет для синхронизации IEEP
        INSTANCE.registerMessage(PacketSyncPlayer.MessageHandler.class, PacketSyncPlayer.class, discriminator++, Side.CLIENT);

        // Пакеты для синхронизации MainContainer'а
        INSTANCE.registerMessage(PacketContainerContent.MessageHandler.class, PacketContainerContent.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketContainerChange.CommonSlotMessageHandler.class, PacketContainerChange.class, discriminator++, Side.CLIENT);
        // Регистрируем сообщения для библиотеки MinecraftTabInventory
        NetworkUtils.registerMessages(INSTANCE, PacketContainerChange.TabInventorySlotMessageHandler.class, TabInventoryItemsMessage.MessageHandler.class, discriminator++); // TODO: Второй хандлер оставлен по умолчанию, но непонятно что будет, если в моде будет присуствовать и постоянный контейнер с TabInventory, и не постоянный
        // Для синхронизации WeightProvider'а
        INSTANCE.registerMessage(WeightProvider.MessageHandler.class, WeightProvider.SyncMessage.class,discriminator, Side.CLIENT);

        // Регистрация предметов
        StatItems.registerItems();
        SkillItems.registerItems();
        OtherItems.registerItems();
        MiscItems.registerItems();
        PerkItems.registerItems();
        DebugItems.registerDebugItems();

        // Регистрация блоков
        Blocks.registerBlocks();

        // Это не срабатывает. Скорее всего, это решение предназначено для более поздних версий Forge
        /*UpgradeStationBlock block3DWeb = (Block3DWeb)(new Block3DWeb().setUnlocalizedName("mbe05_block_3d_web_unlocalised_name"));
        block3DWeb.setRegistryName("mbe05_block_3d_web_registry_name");
        ForgeRegistries.BLOCKS.register(block3DWeb);

        // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
        ItemBlock itemBlock3DWeb = new ItemBlock(block3DWeb);
        itemBlock3DWeb.setRegistryName(block3DWeb.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock3DWeb);*/
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        registerKeyBindings();
    }

    public void postInit(FMLPostInitializationEvent event) {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case RSStats.GUI:
                return ExtendedPlayer.get(player).mainContainer;
            case RSStats.SSP_UI_CODE:
                return new StatsContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory);
            case RSStats.UPGRADE_UI_FROM_BLOCK_CODE: {
                // Получение сущности по координатам блока, по которому кликнул игрок
                TileEntity tileEntity = world.getTileEntity(x, y, z);
                if (tileEntity instanceof UpgradeStationEntity) {
                    UpgradeStationEntity upgradeStationEntity = (UpgradeStationEntity) tileEntity;
                    return new UpgradeContainer(player.inventory, upgradeStationEntity.upgradeStationInventory);
                }
                break;
            }
            case RSStats.UPGRADE_UI_FROM_CMD_CODE:
                return new UpgradeContainer(player.inventory, null);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null; // Переопределяется в ClientProxy
    }
    
    // Переопределяется в ClientProxy
    public void registerKeyBindings() {}
}
