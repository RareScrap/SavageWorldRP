package rsstats.inventory.tabs_inventory;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import rsstats.data.ExtendedPlayer;

// TODO: Класс сделать абстрактным, а реализацию onMessage() поручить наследникам
public class TabMessageHandler implements IMessageHandler<TabHostInventory.SetCurrentTabPacket, IMessage> {

    public TabMessageHandler() {
    }

    @Override
    public IMessage onMessage(TabHostInventory.SetCurrentTabPacket message, MessageContext ctx) {
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(ctx.getServerHandler().playerEntity);

        extendedPlayer.otherTabsInventory.setCurrentTab(message.newCurrentTabName);

        return null;
    }
}
