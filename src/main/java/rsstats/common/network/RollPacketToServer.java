package rsstats.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItem;
import rsstats.roll.DiceRoll;
import rsstats.roll.Result;
import rsstats.roll.RollModifier;
import rsstats.utils.Utils;
       
/**
 * Этот пакет отсылается сервера
 * @author RareScrap
 */
public class RollPacketToServer implements IMessage {
    /** Имя игрока, делающий бросок */
    private String playerName;
    /** UnlocalizedName пробрасываемой статы
     * @see StatItem#unlocalizedName */
    private String rollName;
    /** Определяет стоит ли включить в результаты броска Дикий Кубик */
    private boolean withWildDice;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчике создать объект и распаковать в него буффер. 
     */
    public RollPacketToServer() {}

    public RollPacketToServer(String playerName, String rollName, boolean withWildDice) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.withWildDice = withWildDice;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerName = ByteBufUtils.readUTF8String(buf);
        rollName = ByteBufUtils.readUTF8String(buf);
        withWildDice = ByteBufUtils.readVarShort(buf) == 1; // 1 - true, 0 (or other) - false
    }

    /**
     * ВНИМАНИЕ: в {@link #fromBytes(io.netty.buffer.ByteBuf)} нужно читать данные в
     * порядке их записи в {@link #toBytes(io.netty.buffer.ByteBuf)}!
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, playerName);
        ByteBufUtils.writeUTF8String(buf, rollName);
        ByteBufUtils.writeVarShort(buf, withWildDice ? 1 : 0);
    }
    
    /**
     * Этот внутренний класс обрабатывает пришедший пакет НА СТОРОНЕ СЕРВЕРА
     */
    public static class MessageHandler implements IMessageHandler<RollPacketToServer, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {} // TODO: Зачем?

        @Override
        public IMessage onMessage(RollPacketToServer message, MessageContext ctx) {
            // Находим целевого игрока
            EntityPlayerMP entityPlayerMP = ctx.getServerHandler().playerEntity; // This is the player the packet was sent to the server from
            if (!entityPlayerMP.getDisplayName().equals(message.playerName)) { // TODO: Не знаю, сработает ли это когда-нибудь
                entityPlayerMP = (EntityPlayerMP) entityPlayerMP.worldObj.getPlayerEntityByName(message.playerName); // TODO: Можно ли обойтись без каста?
                if (entityPlayerMP == null) {
                    // Отсылаем на клиент сообщение об ошибке, которое там же локализируется
                    entityPlayerMP.addChatMessage(new ChatComponentTranslation("user_not_found", message.playerName));
                    return null;
                }
            }

            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(entityPlayerMP);

            // Ищем у него стак с указанным итемом
            ItemStack statStack = Utils.findIn(extendedPlayer.statsInventory, message.rollName);
            if (statStack == null) {
                statStack = Utils.findIn(extendedPlayer.skillsInventory, message.rollName);
                if (statStack == null) {
                    entityPlayerMP.addChatMessage(new ChatComponentTranslation("stat_not_found", message.rollName, message.playerName));
                    return null;
                }
            }

            // Формируем бросок
            DiceRoll diceRoll = (DiceRoll) new DiceRoll(entityPlayerMP, statStack).withWildDice(message.withWildDice);

            // Пробрасываем бросок
            Result result = diceRoll.roll();

            // Компонент с модификаторами
            ChatComponentText modifiersComp = new ChatComponentText("<MODIFIERS>");
            for (RollModifier modifier : result.modifiers) {
                modifiersComp.appendSibling(new ChatComponentText(
                        String.format("(%+d: %2$s) ", // TODO: Не нравитсямне добавление пробела в конце
                                modifier.getValue(),
                                modifier.getDescription())
                ));
            }

            // Формируем финальный компонент
            ChatComponentTranslation resultStr;
            if (message.withWildDice) {
                resultStr = new ChatComponentTranslation(
                        "item.StatItem.rollChatMessage_withWildDice",
                        message.playerName,
                        new ChatComponentTranslation(message.rollName+".name"),
                        Utils.getBasicRollFrom(statStack).dice,
                        result.toString(Result.RollType.MAIN),
                        result.toString(Result.RollType.WILD),
                        modifiersComp,
                        result.getTotal(result.getMax(),true)
                );
            } else {
                resultStr = new ChatComponentTranslation(
                        "item.StatItem.rollChatMessage",
                        message.playerName,
                        new ChatComponentTranslation(message.rollName+".name"),
                        Utils.getBasicRollFrom(statStack).dice,
                        result.toString(Result.RollType.MAIN),
                        modifiersComp,
                        result.getTotal(result.getMax(),true)
                );
            }

            // Отправить сообщение в чат на все клиенты игроков
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(resultStr);

            // Для дебага: вывести сторону в пользовательский канал
            //serverPlayer.addChatComponentMessage(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            // вывести сторону в серверный канал
            //FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(new ChatComponentText(FMLCommonHandler.instance().getEffectiveSide().name()));

            return null; // TODO: Скорее всего, стоит отправлять результат ролла на клиент
        }
    }

}