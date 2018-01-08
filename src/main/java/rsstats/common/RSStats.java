package rsstats.common;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import rsstats.client.gui.SSPPage;
import rsstats.common.command.AddLevel;
import rsstats.common.command.ParamsPlayer;
import rsstats.common.event.TestEventHandler;

import java.io.File;

/**
 * Главный класс мода. Представляет собой основу для всех остальных РП модов.
 * Реализует главное меню для UI других РП модов. Предоставляет API создания
 * "страниц" меню, которые затем помещаются на хост-мод (этот мод)
 * @author rares
 */
@Mod(modid = RSStats.MODID, version = RSStats.VERSION)
public class RSStats {
    /** ID мода */
    public static final String MODID = "rsstats";
    /** Имя мода */
    public static final String MODNAME = "RS Stats";
    /** Версия мода */
    public static final String VERSION = "0.0.1a";
    
    /** ID тестового UI (первый UI, который я сделал) */
    public static final int GUI = 0;
    /** ID интерфейса {@link SSPPage} */
    public static final int SSP_UI_CODE = 1;
    
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
    
    /**
     * Хандлер для событий (хз каких)
     * Думаю, он нужен для связывания своих NBT с игрой
     * TODO: разобраться
     */
    public TestEventHandler testEventHandler; 
    
    /** Общий прокси */
    @SidedProxy(clientSide = "rsstats.client.ClientProxy", serverSide = "rsstats.common.CommonProxy")
    public static CommonProxy proxy;

    /** Объект, регистриующий сообщения, которыми обмениваются клиент и сервер */
    //public static SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(MODID);

    public static Config config;

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
        testEventHandler = new TestEventHandler();
        MinecraftForge.EVENT_BUS.register(testEventHandler);

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

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        // register server commands
        event.registerServerCommand(new AddLevel());
        //event.registerServerCommand(new OpenInventory());
        event.registerServerCommand(new ParamsPlayer());

        // Создаем gamerule, который будет сохранять предметы в инвентарях мода после смерти
        MinecraftServer.getServer().worldServerForDimension(0).getGameRules().addGameRule("keepStats", "true");
    }
}
