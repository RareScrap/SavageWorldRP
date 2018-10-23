package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import rsstats.data.ExtendedPlayer;

/**
 * Пакет, синхронизирующий некоторый поля {@link ExtendedPlayer}'а с ExtendedPlayer'ом на клиенте.
 */
public class PacketSyncPlayer implements IMessage {
    private static int BUFFER_INT_SIZE = 1;

    /** Основной параметр игрока - Защита */
    private int protection;
    /** Основной параметр игрока - Стойкость */
    private int persistence;
    /** Объект, хранящий распакованный уровень игрока */
    private int lvl;

    /**
     * Необходимый пустой публичный конструктор
     */
    public PacketSyncPlayer() {}

    public PacketSyncPlayer(ExtendedPlayer player) {
        this.lvl = player.getLvl();
        this.protection = player.getProtection();
        this.persistence = player.getPersistence();
    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        lvl = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        protection = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        persistence = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, lvl, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, protection, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, persistence, BUFFER_INT_SIZE);
    }

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketSyncPlayer, IMessage> {
        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketSyncPlayer message, MessageContext ctx) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            extendedPlayer.setLvl(message.lvl);
            extendedPlayer.setPersistence(message.persistence);
            extendedPlayer.setProtection(message.protection);
            //extendedPlayer.updateParams();
            return null;
        }
    }
}
