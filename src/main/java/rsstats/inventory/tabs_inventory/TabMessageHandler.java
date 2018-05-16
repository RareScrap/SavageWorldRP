package rsstats.inventory.tabs_inventory;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import rsstats.data.ExtendedPlayer;

public class TabMessageHandler implements IMessageHandler<TabHostInventory.SetCurrentTabPacket, IMessage> {

    public TabMessageHandler() {
    }

    @Override
    public IMessage onMessage(TabHostInventory.SetCurrentTabPacket message, MessageContext ctx) {
        ExtendedPlayer.get(ctx.getServerHandler().playerEntity).otherTabsInventory.setCurrentTab(message.newCurrentTabName);
        return null;
    }
}
