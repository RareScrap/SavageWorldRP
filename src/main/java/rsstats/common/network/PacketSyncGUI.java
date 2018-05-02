package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import rsstats.inventory.container.UpgradeContainer;

// TODO: Сделать универсальнм для синхронизации других GUI
/**
 * Синхронизирует отправляет данные с полей {@link rsstats.client.gui.UpgradeGUI} на сервер
 */
public class PacketSyncGUI implements IMessage {
    private static int BUFFER_INT_SIZE = 4;

    /** Описание улучшения, полученное из {@link rsstats.client.gui.UpgradeGUI#descriptionTextField} */
    private String description;
    /** TODO */
    private int value;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчике создать объект и распаковать в него буффер.
     */
    public PacketSyncGUI() {
    }

    /**
     * Конструктор
     * @param description Описание улучшения
     * @param value значения фодификатора
     */
    public PacketSyncGUI(String description, int value) {
        this.description = description;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.description = ByteBufUtils.readUTF8String(buf);
        int type = ByteBufUtils.readVarShort(buf);
        if (type == 1) { // Достаем знак модификатора
            type = -1;
        } else {
            type = 1;
        }
        this.value = type * ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, description);
        ByteBufUtils.writeVarShort(buf, value < 0 ? 1 : 0); // Пересылаем знак
        ByteBufUtils.writeVarInt(buf, Math.abs(value), BUFFER_INT_SIZE);
    }

    public static class MessageHandler implements IMessageHandler<PacketSyncGUI, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {}

        @Override
        public IMessage onMessage(PacketSyncGUI message, MessageContext ctx) {
            Container container = ctx.getServerHandler().playerEntity.openContainer;
            if (container instanceof UpgradeContainer) {
                ((UpgradeContainer) container).updateFields(message.description, message.value);
                ((UpgradeContainer) container).updateUpgradeOutput();
            }

            return null;
        }
    }
}
