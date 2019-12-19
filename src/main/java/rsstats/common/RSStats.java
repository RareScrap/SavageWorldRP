package rsstats.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import rsstats.blocks.UpgradeStationBlock;
import rsstats.client.gui.SSPPage;
import rsstats.common.command.AddLevel;
import rsstats.common.command.Card;
import rsstats.common.command.OpenWindow;
import rsstats.common.command.ParamsPlayer;
import rsstats.common.event.ModEventHandler;
import rsstats.weight.WeightProvider;
import ru.rarescrap.weightapi.WeightRegistry;

import java.io.File;

/**
 * Главный класс мода. Представляет собой основу для всех остальных РП модов.
 * Реализует главное меню для UI других РП модов. Предоставляет API создания
 * "страниц" меню, которые затем помещаются на хост-мод (этот мод)
 * @author rares
 */
@Mod(modid = RSStats.MODID, version = RSStats.VERSION, dependencies = "required-after:weightapi@[0.5.0];required-after:configurableweight@[0.5.1]")
public class RSStats {
    /** ID мода */
    public static final String MODID = "rsstats";
    /** Имя мода */
    public static final String MODNAME = "RS Stats";
    /** Версия мода */
    public static final String VERSION = "0.0.1a";
    
    /** ID интерфейса для панели информации о персонаже - {@link rsstats.client.gui.MainMenuGUI} */
    public static final int GUI = 0;
    /** ID интерфейса {@link SSPPage} */
    public static final int SSP_UI_CODE = 1;
    /** ID интерфейса для {@link rsstats.client.gui.UpgradeGUI} от блока {@link UpgradeStationBlock} */
    public static final int UPGRADE_UI_FROM_BLOCK_CODE = 2;
    /** ID интерфейса для {@link rsstats.client.gui.UpgradeGUI}, запускаемого из команды консоли ({@link OpenWindow}) */
    public static final int UPGRADE_UI_FROM_CMD_CODE = 3;
    
    /** Объект-экземпляр мода */
    @Mod.Instance(MODID)
    public static RSStats instance = new RSStats();

    /** Вкладка в креативе */
    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MODNAME) {
        @Override
        public Item getTabIconItem() {
            return GameRegistry.findItem(RSStats.MODID, "RerollCoinItem");
        }
    };
    
    /** Обработчик игровых событий */
    public ModEventHandler modEventHandler;
    
    /** Общий прокси */
    @SidedProxy(clientSide = "rsstats.client.ClientProxy", serverSide = "rsstats.common.CommonProxy")
    public static CommonProxy proxy;

    /** Объект, регистриующий сообщения, которыми обмениваются клиент и сервер */
    //public static SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(MODID);

    public static Config config;
    public static WeightProvider WEIGHT_PROVIDER;

    /**
     * Конструктор, инициализирующий список допустимых дайсов
     */
    public RSStats() {}

    /**
     * Фаза преинициализации мода. Тут регистрируются предметы, блоки и сообщения
     * @param event Объект события преинициализации
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        modEventHandler = new ModEventHandler();
        MinecraftForge.EVENT_BUS.register(modEventHandler);
        FMLCommonHandler.instance().bus().register(modEventHandler);

        // Обрабатываем конфиг
        config = Config.getConfig(new File(Loader.instance().getConfigDir(), MODNAME+".cfg"));

        proxy.preInit(event); // Преинициализация в общем прокси
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {	
	    proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    // Читам конфиг веса на сервере
    @EventHandler
    public void onServerStart(FMLServerAboutToStartEvent event) {
        File configFile = new File(Loader.instance().getConfigDir(), MODID+"_weight.cfg");
        if (configFile.exists()) {
            WeightRegistry.registerWeightProvider(MODID, WEIGHT_PROVIDER = new WeightProvider(configFile));
        } else throw new RuntimeException("["+MODNAME+"] Can't find config file. Weights not loaded!");
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        // register server commands
        event.registerServerCommand(new AddLevel());
        //event.registerServerCommand(new OpenInventory());
        event.registerServerCommand(new ParamsPlayer());
        event.registerServerCommand(new Card());
        event.registerServerCommand(new OpenWindow());

        // Создаем gamerule, который будет сохранять предметы в инвентарях мода после смерти
        GameRules gameRules = MinecraftServer.getServer().worldServerForDimension(0).getGameRules();
        if (!gameRules.hasRule("keepStats")) {
            gameRules.addGameRule("keepStats", "true");
        }
    }
}
