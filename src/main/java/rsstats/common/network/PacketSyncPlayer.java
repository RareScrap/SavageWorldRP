package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import rsstats.data.ExtendedPlayer;
import rsstats.items.perk.PerkItem;

import java.util.ArrayList;

import static rsstats.data.ExtendedPlayer.Rank;

/**
 * Пакет, синхронизирующий некоторый поля {@link ExtendedPlayer}'а с ExtendedPlayer'ом на клиенте.
 */
public class PacketSyncPlayer implements IMessage {
    private static int BUFFER_INT_SIZE = 1;

    /** Основной параметр игрока - Защита */
    private int protection;
    /** Основной параметр игрока - Стойкость */
    private int persistence;
    /** Основной параметр игрока - Харизма */
    private int charisma;
    /** Ранг игрока */
    private Rank rank;

    /**
     * Необходимый пустой публичный конструктор
     */
    public PacketSyncPlayer() {}

    public PacketSyncPlayer(ExtendedPlayer player) {
        this.rank = player.getRank();
        this.protection = player.getProtection();
        this.persistence = player.getPersistence();
        this.charisma = player.getCharisma();

    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        rank = Rank.fromInt(ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE));
        protection = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        persistence = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        charisma = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, rank.toInt(), BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, protection, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, persistence, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, charisma, BUFFER_INT_SIZE);

    }

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketSyncPlayer, IMessage> {
        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketSyncPlayer message, MessageContext ctx) {
            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            extendedPlayer.setProtection(message.protection);
            extendedPlayer.setPersistence(message.persistence);
            extendedPlayer.setCharisma(message.charisma);
            extendedPlayer.setRank(message.rank);

            return null;
        }

        // Оставлен т.к. понадобится, если вдруг придется синхронизировать модификаторы
        /**
         * Извлекает из стаков с PerkItem'ами модификаторы и копирует их в modifierManager клиента. Сами стаки
         * ни в какой нинветарь не помещаются.
         */
        public void setPerkModifiers(ExtendedPlayer extendedPlayer, ArrayList<ItemStack> perkModifiers) {
            extendedPlayer.modifierManager.clear(); // Очищаем уже имеющиеся модификаторы // TODO: А что если на клиенте есть модификаторы от брони или из других источников?
            for (ItemStack perkStack : perkModifiers) {
                if (perkStack == null) continue;

                PerkItem perkItem = (PerkItem) perkStack.getItem();
                extendedPlayer.modifierManager.addModifiers(perkItem.getModifiers());
            }
        }
    }
}
