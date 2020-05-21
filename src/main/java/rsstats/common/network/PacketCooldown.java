package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import rsstats.data.ExtendedPlayer;

public class PacketCooldown implements IMessage {
    private NBTTagCompound cooldowns;

    // Чтобы отсылать все кулдауны на клиент
    public PacketCooldown(NBTTagCompound cooldowns) {
        this.cooldowns = cooldowns;
    }

    public PacketCooldown() {} // for reflection newInstance

    @Override
    public void fromBytes(ByteBuf buf) {
        cooldowns = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, cooldowns);
    }

    public static class MessageHandler implements IMessageHandler<PacketCooldown, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketCooldown message, MessageContext ctx) {
            ExtendedPlayer player = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            player.cooldownManager.parseSyncNBt(message.cooldowns);
            return null;
        }
    }
}