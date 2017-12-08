/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import rsstats.client.gui.SSPPage;
import rsstats.common.RSStats;

/**
 * Пакет, отправляющийся на сервер при открыти {@link SSPPage}
 * @author RaerScrap
 */
public class PacketOpenSSPPage implements IMessage {
    /**
     * Необходимый пустой публичный конструктор
     */
    public PacketOpenSSPPage() {}

    public PacketOpenSSPPage(EntityPlayer player) {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}
    
    /**
     * Обработчик пакета {@link PacketOpenSSPPage}
     */
    public static class MessageHandler implements IMessageHandler<PacketOpenSSPPage, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenSSPPage message, MessageContext ctx) {
            ctx.getServerHandler().playerEntity.openGui(RSStats.instance, RSStats.SSP_UI_CODE, ctx.getServerHandler().playerEntity.worldObj, (int)ctx.getServerHandler().playerEntity.posX, (int)ctx.getServerHandler().playerEntity.posY, (int)ctx.getServerHandler().playerEntity.posZ);
            return null;
        }
        
    }
    
}
