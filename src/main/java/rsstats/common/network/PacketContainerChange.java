package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.SetTabSlotMessage;
import ru.rarescrap.tabinventory.network.syns.Change;

/**
 * Пакет, уведомляющий клиент об изменении контента {@link MainContainer}'а в каком-либо слоте, который
 * НЕ присоединен к {@link TabInventory}.
 * @see ru.rarescrap.tabinventory.network.SetTabSlotMessage
 * @see MainContainer#detectAndSendChanges()
 */
public class PacketContainerChange implements IMessage {

    //int windowId; // TODO: Нужно ли?
    /** Актуальный стак */
    public ItemStack itemStack;
    /** Слот в клиентском {@link MainContainer}'е, куда следует поместить {@link #itemStack} */
    public int position;

    // for reflection newInstance
    public PacketContainerChange() {}

    public PacketContainerChange(ItemStack itemStack, int postition) {
        this.itemStack = itemStack;
        this.position = postition;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        itemStack = ByteBufUtils.readItemStack(buf);
        position = ByteBufUtils.readVarInt(buf, 1);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, itemStack);
        ByteBufUtils.writeVarInt(buf, position, 1);
    }

    public static class CommonSlotMessageHandler implements IMessageHandler<PacketContainerChange, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(PacketContainerChange m, MessageContext ctx) {
            ExtendedPlayer player = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            player.mainContainer.putStackInSlot(m.position, m.itemStack);
            player.updateParams();
            return null;
        }
    }

    public static class TabInventorySlotMessageHandler implements IMessageHandler<SetTabSlotMessage, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(SetTabSlotMessage message, MessageContext ctx) {

            ExtendedPlayer player = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            //if (player.openContainer.windowId == message.windowId) {
            MainContainer container = player.mainContainer;
            Change change = message.change;

            // Отсылаем ивент о хандле сообщения
            MinecraftForge.EVENT_BUS.post(new SetTabSlotMessage.Event(player.getEntityPlayer(), change));

            // Устанавливает серверный стак в слот на клиентском контейнере
            TabInventory tabInventory = container.getTabInventory(change.inventoryName);
            tabInventory.getTab(change.tabName).setSlotContent(change.slotIndex, change.actualItemStack);
            //}

            // Пересчитывает параметры игрока на клинте
            player.updateParams();

            return null;
        }
    }
}
