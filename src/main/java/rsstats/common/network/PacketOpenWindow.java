package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import rsstats.common.RSStats;

/**
 * Пакет для открытия GUI через команды консоли или кликом по блоку
 */
// TODO: Может вообще отказаться от этой команды?
public class PacketOpenWindow implements IMessage {
    private static final int COMMAND_WINDOW = 0;
    private static final int BLOCK_WINDOW = 1;

    private static int BUFFER_INT_SIZE = 5;

    private int x, y, z;
    private int caller;

    public PacketOpenWindow() {
        this.caller = COMMAND_WINDOW;
    }

    public PacketOpenWindow(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.caller = BLOCK_WINDOW;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        caller = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        if (caller == BLOCK_WINDOW) {
            x = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            y = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            z = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, caller, BUFFER_INT_SIZE);
        if (caller == BLOCK_WINDOW) {
            ByteBufUtils.writeVarInt(buf, x, BUFFER_INT_SIZE);
            ByteBufUtils.writeVarInt(buf, y, BUFFER_INT_SIZE);
            ByteBufUtils.writeVarInt(buf, z, BUFFER_INT_SIZE);
        }
    }

    public static class MessageHandler implements IMessageHandler<PacketOpenWindow, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenWindow message, MessageContext ctx) {
            switch (message.caller) {
                case COMMAND_WINDOW: {
                    ctx.getServerHandler().playerEntity.openGui(RSStats.instance, RSStats.UPGRADE_UI_FROM_CMD_CODE, ctx.getServerHandler().playerEntity.worldObj, (int)ctx.getServerHandler().playerEntity.posX, (int)ctx.getServerHandler().playerEntity.posY, (int)ctx.getServerHandler().playerEntity.posZ);
                    break;
                }
                case BLOCK_WINDOW: {
                    ctx.getServerHandler().playerEntity.openGui(RSStats.instance, RSStats.UPGRADE_UI_FROM_BLOCK_CODE, ctx.getServerHandler().playerEntity.worldObj, message.x, message.y, message.z);
                    break;
                }
            }

            return null;
        }
    }
}
