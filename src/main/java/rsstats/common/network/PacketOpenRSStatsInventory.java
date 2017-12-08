package rsstats.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import rsstats.common.RSStats;

public class PacketOpenRSStatsInventory implements IMessage {

    public PacketOpenRSStatsInventory() {}

    public PacketOpenRSStatsInventory(EntityPlayer player) {}

    @Override
    public void toBytes(ByteBuf buffer) {}

    @Override
    public void fromBytes(ByteBuf buffer) {}

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketOpenRSStatsInventory, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenRSStatsInventory message, MessageContext ctx) {
            ctx.getServerHandler().playerEntity.openGui(RSStats.instance, RSStats.GUI, ctx.getServerHandler().playerEntity.worldObj, (int)ctx.getServerHandler().playerEntity.posX, (int)ctx.getServerHandler().playerEntity.posY, (int)ctx.getServerHandler().playerEntity.posZ);
            //ctx.getServerHandler().playerEntity.ope
            return null;
        }
    }
}
