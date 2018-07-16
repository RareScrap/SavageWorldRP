package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class PacketCommandReponse  implements IMessage {
    private static int BUFFER_INT_SIZE = 4;

    private String commandString;
    private int status;
    private String[] args;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчика создать объект и распаковать в него буффер.
     */
    public PacketCommandReponse() {}

    public PacketCommandReponse(String commandName, int status, String... args) {
        this.commandString = commandName;
        this.status = status;
        this.args = args;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {

            commandString = ByteBufUtils.readUTF8String(buf);
            status = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            int size = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
            args = new String[size];
            for (int i = 0; i < size; i++) {
                args[i] = ByteBufUtils.readUTF8String(buf);
            }
        } catch (Exception e) {

        }
    }

    /**
     * ВНИМАНИЕ: в {@link #fromBytes(io.netty.buffer.ByteBuf)} нужно читать данные в
     * порядке их записи в {@link #toBytes(io.netty.buffer.ByteBuf)}!
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteBufUtils.writeUTF8String(buf, commandString);
            ByteBufUtils.writeVarInt(buf, status, BUFFER_INT_SIZE);
            ByteBufUtils.writeVarInt(buf, args.length, BUFFER_INT_SIZE);
            for (String arg : args) {
                ByteBufUtils.writeUTF8String(buf, arg);
            }
        } catch (Exception e) {

        }

    }

    /**
     * Этот внутренний класс обрабатывает пришедший пакет НА СТОРОНЕ КЛИЕТА
     */
    public static class MessageHandler implements IMessageHandler<PacketCommandReponse, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {}

        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketCommandReponse message, MessageContext ctx) {
            if (message.status != 404) {
                String result = String.format(
                        StatCollector.translateToLocal(message.commandString),
                        (Object[]) message.args);

                EntityPlayer entityPlayer = Minecraft.getMinecraft().thePlayer;
                entityPlayer.addChatComponentMessage(new ChatComponentText(result));
            }


            // This is the player the packet was sent to the server from
            //EntityPlayerMP serverPlayer = ctx.getServerHandler().playerEntity;

            /*if (message.diceRollMessage == null)
                throw new NullPointerException("diceRollMessage is null");

            String result = message.diceRollMessage.roll(message.withWildDice);

            // и вывести его в чат
            //serverPlayer.addChatComponentMessage(new ChatComponentText(result));
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(result));
*/
            //serverPlayer.addChatComponentMessage(new ChatComponentText(message.diceRollMessage.dice + " " + message.diceRollMessage.statName));


            // Для дебага: вывести сторону в пользовательский канал
            //serverPlayer.addChatComponentMessage(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            // вывести сторону в серверный канал
            //FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            return null;
        }
    }

}
