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
import rsstats.items.perk.PerkItem;

import java.util.ArrayList;

import static rsstats.data.ExtendedPlayer.Rank;

/**
 * Пакет, синхронизирующий базовые значение параметров {@link ExtendedPlayer}'а с ExtendedPlayer'ом на клиенте.
 */
// Модификаторы к параметрам синхронизирует MainContainer, т.к. это просто итемы
public class PacketSyncPlayer implements IMessage {
    private static int BUFFER_INT_SIZE = 1;

    private int step;
    private int protection;
    private int persistence;
    private int charisma;
    private Rank rank;
    // Еще не определился какие значения нужны, так что синхроню то, что пока использую

    // for reflection newInstance
    public PacketSyncPlayer() {}

    public PacketSyncPlayer(ExtendedPlayer player) {
        this.step = player.step;
        this.protection = player.protection;
        this.persistence = player.persistence;
        this.charisma = player.charisma;
        this.rank = player.rank;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        step = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        protection = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        persistence = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        charisma = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        rank = Rank.fromInt(ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, step, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, protection, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, persistence, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, charisma, BUFFER_INT_SIZE);
        ByteBufUtils.writeVarInt(buf, rank.toInt(), BUFFER_INT_SIZE);
    }

    public static class MessageHandler implements IMessageHandler<PacketSyncPlayer, IMessage> {
        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketSyncPlayer m, MessageContext ctx) {
            ExtendedPlayer player = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            player.step = m.step;
            player.protection = m.protection;
            player.persistence = m.persistence;
            player.rank = m.rank;
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
