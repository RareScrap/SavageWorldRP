package rsstats.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.GameRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.roll.DiceRoll;
import rsstats.roll.Result;
import rsstats.roll.RollModifier;
import rsstats.utils.Utils;

/**
 * Пакет, побуждающий сервер произвести проброс статы/скилла
 * @author RareScrap
 */
public class RollPacketToServer implements IMessage {
    /** Имя игрока, делающий бросок */
    private String playerName; // TODO: Зачем оно нужно, ведь можно получить его из ctx?
    /** Уникальное имя пробрасываемой статы
     * @see cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier#name */
    private String rollName;
    /** Стоит ли включить в результаты броска Дикий Кубик */
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
            ItemStack statStack = findRollStack(extendedPlayer, m.rollName);
            if (statStack == null) {
                entityPlayerMP.addChatMessage(new ChatComponentTranslation(
                        "stat_not_found",
                        m.rollName,
                        m.playerName));
                return null;
            }

            // Формируем бросок
            DiceRoll diceRoll = (DiceRoll) new DiceRoll(entityPlayerMP, statStack).withWildDice(m.withWildDice);
            Result result = diceRoll.roll(); // Пробрасываем бросок
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
         * @param rollName Уникальное имя статы/навыка
         * @return Стак с итемом статы/навыка. Null, если ничего не найдено.
         */
        private ItemStack findRollStack(ExtendedPlayer player, String rollName) {
            // Ищем среди статов
            ItemStack statStack = Utils.findIn(player.statsInventory, rollName);
            if (statStack != null) return statStack;

            // Если не нашли - среди скиллов

            // Чтоб не перебирать все вкладки инвентаря, находим итем скилла по указанному имени ... // TODO: Замерить производительность при переборе и сравнить с текущим решением
            SkillItem rollItem = (SkillItem) GameRegistry.findItem(RSStats.MODID, rollName);
            // А потом из итема достаем стату-родитель и получаем ее имя, которое используется как ключ вкладки
            String parentStatName = GameRegistry.findUniqueIdentifierFor(rollItem.parentStat).name;

            statStack = ru.rarescrap.tabinventory.utils.Utils.findIn(
                    player.skillsInventory,
                    rollName,
                    "item."+parentStatName); // TODO: Ебанный костыль

            return statStack;
        }

        /**
         * Создает компонент чата, содержащий в себе модификаторы результата ролла
         */
        private ChatComponentText createModifierComponent(Result rollResult) {
            // Если модификаторов нет, вернем "пустой" компонент который не оторазится в чате
            if (rollResult.modifiers.isEmpty()) return new ChatComponentText("");

            ChatComponentText modifiersComp = new ChatComponentText("<MODIFIERS>"); // Маркируем компонент, чтобы клиент мог его найти
            for (RollModifier modifier : rollResult.modifiers) {

                // Добавляем модификаторы к компоненту
                modifiersComp.appendSibling(new ChatComponentText(
                        String.format("(%+d: %2$s) ", // TODO: Не нравитсямне добавление пробела в конце и почему шаблон берется не из файлов локализации?
                        modifier.value,
                        modifier.description)
                ));

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