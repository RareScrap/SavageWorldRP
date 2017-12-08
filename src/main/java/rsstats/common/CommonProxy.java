/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import static rsstats.common.RSStats.instance;
import static rsstats.common.RSStats.proxy;
import rsstats.common.event.KeyHandler;
import rsstats.common.network.PacketOpenRSStatsInventory;
import rsstats.common.network.PacketOpenSSPPage;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import rsstats.inventory.container.StatsContainer;

/**
 * Проки, содержащий код как для клиента, так и сервера
 * @author RareScrap
 */
public class CommonProxy implements IGuiHandler {
    /** Обработчик нажатия кнопок, используемых для вызова GUI */
    protected KeyHandler keyHandler;
    /** Обертка для работы с сетью */
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(RSStats.MODID.toLowerCase());

    void preInit(FMLPreInitializationEvent event) {
        // Когда сообщений станет много, их можно вынести в отдельный класс в метод init()
        INSTANCE.registerMessage(PacketOpenRSStatsInventory.MessageHandler.class, PacketOpenRSStatsInventory.class, 0, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenSSPPage.MessageHandler.class, PacketOpenSSPPage.class, 1, Side.SERVER);
    }

    void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        registerKeyBindings();
    }

    void postInit(FMLPostInitializationEvent event) {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        System.out.print("PIZDA1"+ID+"\n");
        switch (ID) {
            case RSStats.GUI:
                return new MainContainer(player, player.inventory);
            case RSStats.SSP_UI_CODE:
                return new StatsContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory);

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
