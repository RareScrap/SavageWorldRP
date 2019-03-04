package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import rsstats.data.ExtendedPlayer;

/**
 * Сообщение, информирующее сервер о том, что была нажата одна из кнопок в
 * {@link rsstats.client.gui.advanced.Dialog}
 */
public class PacketDialogAction implements IMessage {
    /** Типы нажатой кнопки */
    public enum ActionType{
        /** Положительное действие (ответ "да") */
        POSITIVE,
        /** Отрицательное действие (ответ "нет"), которое, как правило, возвращает пользователя назад без отбрасывания
         * каких-либо данных. */
        NNEGATIVE,
        /** Действие отмены. Как правило означает "выйти и отбросить все данные". */
        CANCEL
    }

    /** Тип нажатой кнопки */
    public ActionType actionType;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне обработчика создать объект и распаковать в него буффер.
     * <strong>НЕ ИСПОЛЬЗУЙТЕ ЕГО САМОСТОЯТЕЛЬНО!</strong>
     * @see io.netty.handler.codec.MessageToMessageDecoder
     */
    public PacketDialogAction() {
    }

    /**
     * Конструктор
     * @param actionType Тип нажатой кнопки
     */
    public PacketDialogAction(ActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int actionTypeOrdinal = ByteBufUtils.readVarShort(buf);
        actionType = ActionType.values()[actionTypeOrdinal];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, actionType.ordinal());
    }

    /** Обработчик сообщения. Обрабатывающая сторона определена при регистрации сообщения. */
    public static class MessageHandler implements IMessageHandler<PacketDialogAction, IMessage> {
        @Override
        public IMessage onMessage(PacketDialogAction message, MessageContext ctx) {
            switch (message.actionType) {
                case CANCEL: {
                    // Восстанавливаем статы и навыки
                    ExtendedPlayer.get(ctx.getServerHandler().playerEntity).levelupManager.restoreBild();
                    break;
                }
            }
            return null;
        }
    }
}
