package rsstats.client;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import rsstats.client.gui.MainMenuGUI;
import rsstats.client.gui.SSPPage;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.event.KeyHandler;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;

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
                case RSStats.GUI: return new MainMenuGUI(ExtendedPlayer.get(player), new MainContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory, ExtendedPlayer.get(player).skillsInventory));
                /*
                ВНИМАНИЕ! По туториалу, мне не нужно делать проверку в строке 25.
                Более того, мне нужно свитч затолкать в CommonProxy. Но так как
                у меня все работает и при таком раскладе, я пока оставлю все как есть
                */
                case RSStats.SSP_UI_CODE: return new SSPPage(player, player.inventory, ExtendedPlayer.get(player).statsInventory);
            }
        }
        return null;
    }
    
    // Кей-хандлеры должны регистрироваться только на клиенте
    @Override
    public void registerKeyBindings() {
        keyHandler = new KeyHandler();
        FMLCommonHandler.instance().bus().register(keyHandler);
        MinecraftForge.EVENT_BUS.register(new MainMenuGUI(null, new MainContainer()));
    }
}
