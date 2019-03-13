/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.common.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import rsstats.data.ExtendedPlayer;
import rsstats.items.OtherItems;
import rsstats.roll.RollModifier;

import java.util.List;

/**
 *
 * @author rares
 */
public class ModEventHandler {
    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
    /* 
    Be sure to check if the entity being constructed is the correct type for the
    extended properties you're about to add! The null check may not be
    necessary - I only use it to make sure properties are only registered
    once per entity
    */
    if (event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer) event.entity) == null)
        // This is how extended properties are registered using our convenient method from earlier
        ExtendedPlayer.register((EntityPlayer) event.entity);
        // That will call the constructor as well as cause the init() method
        // to be called automatically

    // If you didn't make the two convenient methods from earlier, your code would be
    // much uglier:
    //if (event.entity instanceof EntityPlayer && event.entity.getExtendedProperties(ExtendedPlayer.EXT_PROP_NAME) == null)
    //event.entity.registerExtendedProperties(ExtendedPlayer.EXT_PROP_NAME, new ExtendedPlayer((EntityPlayer) event.entity));
    }

    /**
     * Синхронизирует данные на клиенте с сервером в момент входа пользователя в игру
     * @param e
     */
    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent e) {
        if (e.entity instanceof EntityPlayer) {
            ExtendedPlayer player = ExtendedPlayer.get((EntityPlayer) e.entity);

            // Альтернативная начальная инициализация вкладок
            if (player.otherTabsHost.isEmpty()) {
                player.otherTabsHost.setInventorySlotContents(0, new ItemStack(OtherItems.perksTabItem, 1));
                player.otherTabsHost.setInventorySlotContents(1, new ItemStack(OtherItems.flawsTabItem, 1));
                player.otherTabsHost.setInventorySlotContents(2, new ItemStack(OtherItems.positiveEffectsTabItem, 1));
                player.otherTabsHost.setInventorySlotContents(3, new ItemStack(OtherItems.negativeEffectsTabItem, 1));
            }

            // TODO: Мне не нравится что контейнер инициализируется снаружи ExtendedPlayer'а. Я думаю, что это источник потенциальных ошибок. Ровно как и из-за альтернативной инициализции я вынужден инициализировать контейнер тут, т.к. инициализация должна происходить только после того как все иначальные итемы будут расставлены. А еще мне не нравится и это требование. Почему итемы нельзя расставлять после инициализации контейнера?
            player.initContainer(); // Включаем постоянную синхронизацию инвентарей (После инициализации вкладок!)

            if (player != null)
                player.sync();
        }
    }

    @SubscribeEvent
    public void onClonePlayer(PlayerEvent.Clone e) {
        // Если игрок умер и включен gamerule, сохраняющий предметы статов после смерти ...
        if(e.wasDeath && MinecraftServer.getServer().worldServerForDimension(0).getGameRules().getGameRuleBooleanValue("keepStats")) {
            // то сохраним их
            NBTTagCompound compound = new NBTTagCompound();
            ExtendedPlayer.get(e.original).saveNBTData(compound);
            ExtendedPlayer.get(e.entityPlayer).loadNBTData(compound);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        processRollChatPacket(event);
    }

    /**
     * Пробует обработать {@link ClientChatReceivedEvent#message} как сообщение о броске кости.
     * @param event События с сообщением, полученное с сервера из
     *              {@link rsstats.common.network.RollPacketToServer.MessageHandler}
     * @return True, если операция прошла успешно. Иначе - false.
     */
    private static boolean processRollChatPacket(ClientChatReceivedEvent event) {
        try { // TODO: Стоит ли создавать свой ChatComponent (или ChatComponentTranslation)?
            // Пытаем кастануть сообщение
            ChatComponentTranslation chatComp = (ChatComponentTranslation) event.message;

            Object[] formatArgs = chatComp.getFormatArgs();
            for (int i = 0; i < formatArgs.length; i++) {
                Object part = formatArgs[i];

                // Находим компонент с модификаторами броска
                if (part instanceof ChatComponentText
                        && ((ChatComponentText) part).getChatComponentText_TextValue().equals("<MODIFIERS>")) {

                    // Заменяем старый компонен на новый, к которому применен конфиг на клиенте
                    formatArgs[i] = processModifiersComponent((ChatComponentText) part);

                    // Повторно локализуем компонент
                    event.message = chatComp.createCopy(); // Думаю, это решение вполне совместимо с другими модами
                    break;
                }
            }

            return true;

        } catch (ClassCastException e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Преобразует чат-компонент с модификаторами в читаемый вид
     * @param modifiersComponent Сырой конпонент с модификаторами
     * @return Локализованные модификаторы с примененными цветами и удаленным текстом-маркером
     */
    private static ChatComponentText processModifiersComponent(ChatComponentText modifiersComponent) {
        // Формируем новый компонент с модификаторами броска, на основе старого
        ChatComponentText returnedComponent = new ChatComponentText("");

        List siblings = modifiersComponent.getSiblings();
        for (int i1 = 0; i1 < siblings.size(); i1+=2) {
            // Данные модификаторов хранятся в сиблингах по 2 объекта на один модификатор
            ChatComponentText modifierValueComponent = (ChatComponentText) siblings.get(i1);
            ChatComponentText modifierDescriptionComponent = (ChatComponentText) siblings.get(i1+1);

            // Создаем объект модификатора
            RollModifier modifier = new RollModifier(
                    Integer.parseInt(modifierValueComponent.getChatComponentText_TextValue()),
                    modifierDescriptionComponent.getChatComponentText_TextValue()
            );

            // Формируем на основе его компонент
            ChatComponentText modifierComponent = new ChatComponentText(modifier.getTranslatedString());
            modifierComponent.appendText(" ");  // И пробел чтобы модификаторы не слиплялись

            if (modifier.value >= 0) { // Применяем пользовательские настройки цвета
                modifierComponent.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN)); // TODO: Юзать конфиг
            } else { // contains("(-")
                modifierComponent.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
            }

            // Присоединяем компонент с модификатором к общему хранилищу
            returnedComponent.appendSibling(modifierComponent);
        }

        return returnedComponent;
    }

    @SubscribeEvent
    public void onUpdatePlayer(TickEvent.PlayerTickEvent event) {
        if (event.player.worldObj.isRemote) return;
        ExtendedPlayer player = ExtendedPlayer.get(event.player);
        if (event.player.openContainer != player.mainContainer)
            player.mainContainer.detectAndSendChanges();
    }
}
