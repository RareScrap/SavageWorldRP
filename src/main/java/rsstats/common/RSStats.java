package rsstats.common;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import rsstats.client.gui.SSPPage;
import rsstats.common.event.TestEventHandler;

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
    
    /**
     * Хандлер для событий (хз каких)
     * Думаю, он нужен для связывания своих NBT с игрой
     * TODO: разобраться
     */
    public TestEventHandler testEventHandler; 
    
    /** Общий прокси */
    @SidedProxy(clientSide = "rsstats.client.ClientProxy", serverSide = "rsstats.common.CommonProxy")
    public static CommonProxy proxy;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        testEventHandler = new TestEventHandler();
        MinecraftForge.EVENT_BUS.register(testEventHandler);
        
        proxy.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {	
	proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
