package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import rsstats.common.RSStats;
import rsstats.inventory.container.MainContainer;

public class PacketShowSkillsByStat implements IMessage {
    private String parentStatName;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчике создать объект и распаковать в него буффер.
     */
    public PacketShowSkillsByStat() {}

    public PacketShowSkillsByStat(String parentStatName) {
        this.parentStatName = parentStatName;
    }

    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        parentStatName = ByteBufUtils.readUTF8String(buf);
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, parentStatName);
    }

    /**
     * Этот внутренний класс обрабатывает пришедший пакет НА СТОРОНЕ СЕРВЕРА
     */
    public static class MessageHandler implements IMessageHandler<PacketShowSkillsByStat, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {}

        @Override
        public IMessage onMessage(PacketShowSkillsByStat message, MessageContext ctx) {
            if (message.parentStatName == null)
                throw new NullPointerException("parentStatName is null");

            MainContainer serverMainContainer = RSStats.proxy.serverMainContainer;
            serverMainContainer.setSkillsFor(message.parentStatName);

            return null;
        }
    }
}
