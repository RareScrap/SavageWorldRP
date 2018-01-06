package rsstats.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChatComponentText;
import rsstats.utils.DiceRoll;
import rsstats.utils.RollModifier;

import java.util.ArrayList;
       
/**
 * Этот пакет отсылается сервера
 * @author RareScrap
 */
public class RollPacketToServer implements IMessage {
    private static int BUFFER_INT_SIZE = 1;
    private DiceRoll diceRollMessage;
    
    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчике создать объект и распаковать в него буффер. 
     */
    public RollPacketToServer() {}

    public RollPacketToServer(DiceRoll message) {
        System.out.print("const");
        this.diceRollMessage = message;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        // Создаем обьект броска и передаем в него дайс для броска
        String playerName = ByteBufUtils.readUTF8String(buf);
        String rollName = ByteBufUtils.readUTF8String(buf);
        int dice = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        
        ArrayList<RollModifier> modificators = new ArrayList<RollModifier>();
        int size = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        for (int i = 0; i < size; i++) {
            int value;
            // Получаем значение, определяющее знак модификатора
            if (ByteBufUtils.readVarShort(buf) == 0) { // 1 - это "+"
                value = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            } else { // 0 - это "-"
                value = -ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            }
            String description = ByteBufUtils.readUTF8String(buf);
            modificators.add(new RollModifier(value, description));
        }
        
        String template = ByteBufUtils.readUTF8String(buf);

        //TODO
        /*if (size > 0)
            this.diceRollMessage = new DiceRoll(playerName, rollName, dice, template);
        else
            this.diceRollMessage = new DiceRoll(playerName, rollName, dice, modificators, template);
        */
        this.diceRollMessage = new DiceRoll(playerName, rollName, dice, modificators, template);
    }

    /**
     * ВНИМАНИЕ: в {@link #fromBytes(io.netty.buffer.ByteBuf)} нужно читать данные в
     * порядке их записи в {@link #toBytes(io.netty.buffer.ByteBuf)}!
     * @param buf 
     */
    @Override
    public void toBytes(ByteBuf buf) {
        // TODO: ОПАСНО! Может оказаться, что одного байта не хватит (последний аргумент)
        ByteBufUtils.writeUTF8String(buf, diceRollMessage.getPlayerName());
        ByteBufUtils.writeUTF8String(buf, diceRollMessage.getRollName());
        ByteBufUtils.writeVarInt(buf, diceRollMessage.getDice(), BUFFER_INT_SIZE);
        
        if (diceRollMessage.getModificators() != null && !diceRollMessage.getModificators().isEmpty()) {
            ByteBufUtils.writeVarInt(buf, diceRollMessage.getModificators().size(), BUFFER_INT_SIZE);
            for (int i = 0; i < diceRollMessage.getModificators().size(); i++) {
                RollModifier modificator = diceRollMessage.getModificators().get(i);
                /* Определяет знак модификатора. Используетяся т.к. writeVarShort не умеет записывать
                 * отрицательные числа. 1 - это "+", 0 - это "-". */
                ByteBufUtils.writeVarShort(buf, modificator.getValue() < 0 ? 1 : 0);
                ByteBufUtils.writeVarInt(buf, Math.abs(modificator.getValue()), BUFFER_INT_SIZE); // Записываем значение
                ByteBufUtils.writeUTF8String(buf, modificator.getDescription()); // Записываем описание
            }
        } else {
            // Записываем размер пустого списка модификаторов
            ByteBufUtils.writeVarInt(buf, 0, BUFFER_INT_SIZE);
        }
        
        ByteBufUtils.writeUTF8String(buf, diceRollMessage.getTemplate());
    }
    
    /**
     * Этот внутренний класс обрабатывает пришедший пакет НА СТОРОНЕ СЕРВЕРА
     */
    public static class MessageHandler implements IMessageHandler<RollPacketToServer, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {}

        @Override
        public IMessage onMessage(RollPacketToServer message, MessageContext ctx) {
            // This is the player the packet was sent to the server from
            //EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;

            if (message.diceRollMessage == null)
                throw new NullPointerException("diceRollMessage is null");

            String result = message.diceRollMessage.roll();

            // и вывести его в чат
            //serverPlayer.addChatComponentMessage(new ChatComponentText(result));
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(result));

            //serverPlayer.addChatComponentMessage(new ChatComponentText(message.diceRollMessage.dice + " " + message.diceRollMessage.statName));


            // Для дебага: вывести сторону в пользовательский канал
            //serverPlayer.addChatComponentMessage(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            // вывести сторону в серверный канал
            //FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            return null;
        }
    }

}