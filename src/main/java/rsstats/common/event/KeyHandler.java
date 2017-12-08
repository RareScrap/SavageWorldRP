package rsstats.common.event;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;
import rsstats.common.CommonProxy;
import rsstats.common.network.PacketOpenRSStatsInventory;
import rsstats.common.network.PacketOpenSSPPage;

/**
 * Обработчик нажатия кнопок, используемых для вызова GUI
 * 
 * В этом классе не реализовано очень много интересных вещей из тутора:
 * https://github.com/coolAlias/Forge_Tutorials/blob/master/CustomPlayerInventory.java
 * 
 * @author RareScrap
 */
public class KeyHandler {
	/** Кей-биндинги */
	public KeyBinding[] keys = {
            // Кнопка открытия тестового UI
            new KeyBinding("keybind.rsstatsinventory", Keyboard.KEY_B, "key.categories.inventory"),
            // Кнопка открытия SSPPage
            new KeyBinding("keybind.ssppage", Keyboard.KEY_N, "key.categories.inventory")
        };
        
        /**
         * Конструктор, регистрирующий кей-биндинги из {@link #keys}
         */
	public KeyHandler() {
            for (KeyBinding key : keys) {
                ClientRegistry.registerKeyBinding(key);
            }
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
            if (event.side == Side.SERVER) return;
            if (event.phase == Phase.START ) {
                if (keys[0].isPressed() && FMLClientHandler.instance().getClient().inGameHasFocus) {
                    CommonProxy.INSTANCE.sendToServer(new PacketOpenRSStatsInventory(event.player));
                }
                if (keys[1].isPressed() && FMLClientHandler.instance().getClient().inGameHasFocus) {
                    CommonProxy.INSTANCE.sendToServer(new PacketOpenSSPPage(event.player));
                }
            }
	}
}

