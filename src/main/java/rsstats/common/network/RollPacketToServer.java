package rsstats.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import rsstats.data.ExtendedPlayer;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.api.roll.PlayerRoll;
import rsstats.api.roll.Result;
import rsstats.api.roll.RollModifier;

/**
 * Пакет, побуждающий сервер произвести проброс статы/скилла
 * @author RareScrap
 */
public class RollPacketToServer implements IMessage {
    /** Имя игрока, делающий бросок */
    private String playerName; // TODO: Зачем оно нужно, ведь можно получить его из ctx?
    /** ID итема пробрасываемой статы */
    private int rollItemId;
    /** Стоит ли включить в результаты броска Дикий Кубик */
    private boolean withWildDice;

    /**
     * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
     * стороне-обработчике создать объект и распаковать в него буффер. 
     */
    public RollPacketToServer() {}

    public RollPacketToServer(String playerName, int rollItemId, boolean withWildDice) {
        this.playerName = playerName;
        this.rollItemId = rollItemId;
        this.withWildDice = withWildDice;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        playerName = ByteBufUtils.readUTF8String(buf);
        rollItemId = ByteBufUtils.readVarShort(buf); // Майн тоже передает ID через short
        withWildDice = ByteBufUtils.readVarShort(buf) == 1; // 1 - true, 0 (or other) - false
    }

    /**
     * ВНИМАНИЕ: в {@link #fromBytes(io.netty.buffer.ByteBuf)} нужно читать данные в
     * порядке их записи в {@link #toBytes(io.netty.buffer.ByteBuf)}!
     */
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, playerName);
        ByteBufUtils.writeVarShort(buf, rollItemId);
        ByteBufUtils.writeVarShort(buf, withWildDice ? 1 : 0);
    }
    
    /**
     * Этот внутренний класс обрабатывает пришедший пакет НА СТОРОНЕ СЕРВЕРА
     */
    public static class MessageHandler implements IMessageHandler<RollPacketToServer, IMessage> {
        // Do note that the default constructor is required, but implicitly defined in this case
        public MessageHandler() {} // TODO: Зачем?

        @Override
        public IMessage onMessage(RollPacketToServer m, MessageContext ctx) {
            // Находим целевого игрока
            EntityPlayerMP entityPlayerMP = ctx.getServerHandler().playerEntity; // This is the player the packet was sent to the server from
            if (!entityPlayerMP.getDisplayName().equals(m.playerName)) { // TODO: Не знаю, сработает ли это когда-нибудь
                entityPlayerMP = (EntityPlayerMP) entityPlayerMP.worldObj.getPlayerEntityByName(m.playerName); // TODO: Можно ли обойтись без каста?
                if (entityPlayerMP == null) {
                    // Отсылаем на клиент сообщение об ошибке, которое там же локализируется
                    entityPlayerMP.addChatMessage(new ChatComponentTranslation("user_not_found", m.playerName));
                    return null;
                }
                throw new RuntimeException("ТЫ НЕ ПОВЕРИШЬ, НО ЭТА СИТУАЦИЯ ВОЗМОЖНА!");
            }

            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(entityPlayerMP);

            // Ищем у него стак с указанным итемом
            Item clickedItem = Item.getItemById(m.rollItemId);
            ItemStack statStack = findRollStack(extendedPlayer, clickedItem);
            if (statStack == null) {
                entityPlayerMP.addChatMessage(new ChatComponentTranslation(
                        "stat_not_found",
                        new ChatComponentTranslation(clickedItem.getUnlocalizedName() + ".name"),
                        m.playerName));
                return null;
            }

            // Формируем бросок
            PlayerRoll playerRoll = (PlayerRoll) new PlayerRoll(ExtendedPlayer.get(entityPlayerMP), statStack).withWildDice(m.withWildDice);
            Result result = playerRoll.roll(); // Пробрасываем бросок
            // Формируем компонент с модификаторами
            ChatComponentText modifiersComp = createModifierComponent(result);
            // Формируем финальный компонент
            ChatComponentTranslation finaleComp = createFinaleComponent(m, statStack, result, modifiersComp);
            // И отправляем его в чат на все клиенты игроков
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(finaleComp);

            return null; // TODO: Скорее всего, стоит отправлять результат ролла на клиент
        }

        /**
         * Находит стак по имени статы/навыка
         * @param player Игрок, в инвентаре которого произвойдет поиск
         * @param rollItem Итем статы/навыка
         * @return Стак с итемом статы/навыка. Null, если ничего не найдено.
         */
        private ItemStack findRollStack(ExtendedPlayer player, Item rollItem) {
            // Ищем среди статов
            if (rollItem instanceof SkillItem) { // TODO: Пора отказаться от наследования в итемах
                return player.getSkill((SkillItem) rollItem);
            } else { // instance of StatItem
                return player.getStat((StatItem) rollItem);
            }
        }

        /**
         * Создает компонент чата, содержащий в себе модификаторы результата ролла
         */
        private ChatComponentText createModifierComponent(Result rollResult) {
            // Если модификаторов нет, вернем "пустой" компонент который не оторазится в чате
            if (rollResult.modifiers.isEmpty()) return new ChatComponentText("");

            ChatComponentText modifiersComp = new ChatComponentText("<MODIFIERS>"); // Маркируем компонент, чтобы клиент мог его найти
            for (RollModifier modifier : rollResult.modifiers) {
                // Данные модификаторов будем слать двумя объектами, прикрепленными в виде сиблингов // TODO: Если слать одним, то придется делать парсинг на клиенте, что подталкиевает к написанию своего ChatComponent'а. А если писать его, то придется еще и либу к нему писать
                modifiersComp.appendText(String.valueOf(modifier.value));
                modifiersComp.appendText(modifier.description);
            }

            return modifiersComp;
        }

        /**
         * Создает итоговый компонент чата, которй будет показан игрокам
         * @param statStack Стак с итемом скилла/статы
         */
        private ChatComponentTranslation createFinaleComponent(RollPacketToServer m, ItemStack statStack, Result rollResult, ChatComponentText modifiersComp) {
            if (m.withWildDice) {
                return new ChatComponentTranslation(
                        "item.StatItem.rollChatMessage_withWildDice",
                        m.playerName,
                        new ChatComponentTranslation(statStack.getUnlocalizedName()+".name"),
                        StatItem.getRoll(statStack).dice,
                        rollResult.toString(Result.RollType.MAIN),
                        rollResult.toString(Result.RollType.WILD),
                        modifiersComp,
                        rollResult.getTotal(rollResult.getMax(),true)
                );
            } else {
                return new ChatComponentTranslation(
                        "item.StatItem.rollChatMessage",
                        m.playerName,
                        new ChatComponentTranslation(statStack.getUnlocalizedName()+".name"),
                        StatItem.getRoll(statStack).dice,
                        rollResult.toString(Result.RollType.MAIN),
                        modifiersComp,
                        rollResult.getTotal(rollResult.getMax(),true)
                );
            }
        }
    }

}