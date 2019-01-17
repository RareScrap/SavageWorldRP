package rsstats.common.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.TabInventoryItemsMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Пакет, синхронизирующий контент {@link MainContainer}'а с MainContainer'ом на клиенте игрока.
 *
 */
public class PacketContainerContent implements IMessage {
    private static int BUFFER_INT_SIZE = 1;

    /** Стаки для каждого слота (кроме слотов, подсоединенных к {@link TabInventory} */
    ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
    /** Хранилище стаков в слотах, связанных с {@link TabInventory} */
    ArrayList<TabInventoryItemsMessage> tabInventoryItems = new ArrayList<TabInventoryItemsMessage>();

    // for reflection newInstance
    public PacketContainerContent() {}

    public PacketContainerContent(MainContainer container) {
        /* Тут будем хранить ссылки на вкладочные инвентари, т.к. их будет удобнее обработать
         * после обработки слотов, которые хранят только один стак. */
        List<TabInventory> tempRefStorage = new ArrayList<TabInventory>();

        List<Slot> slots = container.inventorySlots;
        for (Slot slot : slots) {
            if (slot.inventory instanceof TabInventory) {
                // Т.е. слоты с TabInventory мы не обрабатываем, т.к. удобнее обработать сам инвентарь, а не слот с ним
                if (!tempRefStorage.contains(slot.inventory)) tempRefStorage.add((TabInventory) slot.inventory);
            } else {
                stacks.add(slot.getStack());
            }
        }

        // А теперь извлем стаки из вкладочных инвентарей
        for (TabInventory tabInventory : tempRefStorage)
            // TODO: Зачем windowId если хандлер - кастомный?
            tabInventoryItems.add(new TabInventoryItemsMessage(tabInventory, container.windowId));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        for (int i = 0; i < size; i++)
            stacks.add(ByteBufUtils.readItemStack(buf));

        size = ByteBufUtils.readVarInt(buf, BUFFER_INT_SIZE);
        for (int i = 0; i < size; i++) {
            TabInventoryItemsMessage tabInventoryItemsMessage = new TabInventoryItemsMessage();
            tabInventoryItemsMessage.fromBytes(buf);
            tabInventoryItems.add(tabInventoryItemsMessage);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, stacks.size(), BUFFER_INT_SIZE);
        for (ItemStack itemStack : stacks)
            ByteBufUtils.writeItemStack(buf, itemStack);

        ByteBufUtils.writeVarInt(buf, tabInventoryItems.size(), BUFFER_INT_SIZE);
        for (TabInventoryItemsMessage tabInventoryItemsMessage : tabInventoryItems)
            tabInventoryItemsMessage.toBytes(buf);
    }

    /**
     * Обработчик сообщения {@link PacketOpenRSStatsInventory}
     */
    public static class MessageHandler implements IMessageHandler<PacketContainerContent, IMessage> {
        @Override
        @SideOnly(Side.CLIENT) // Для использования клиенских классов при регистрации пакета на серве
        public IMessage onMessage(PacketContainerContent m, MessageContext ctx) {
            ExtendedPlayer player = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
            MainContainer container = player.mainContainer;

            // Помещаем стаки в слоты клиентского контейнера
            List<Slot> slots = container.inventorySlots;
            for (int i = 0, j = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if ( !(slot.inventory instanceof TabInventory) ) {
                    slot.putStack(m.stacks.get(j++));
                }
            }

            // Записываем стаки во складочные инвентари контейнера
            for (TabInventoryItemsMessage message : m.tabInventoryItems) {
                //if (player.openContainer.windowId == message.windowId) {

                TabInventory tabInventory = container.getTabInventory(message.inventoryName);
                if (tabInventory != null) {

                    // Отсылаем ивент о хандле сообщения
                    MinecraftForge.EVENT_BUS.post(new TabInventoryItemsMessage.Event(
                            player.getEntityPlayer(),
                            tabInventory.getInventoryName(),
                            tabInventory.items,
                            message.items));

                    // Копируем итемы с сервера в клиентский контейнер
                    for (Map.Entry<String, ItemStack[]> entry : message.items.entrySet()) {
                        ItemStack[] stacks = entry.getValue();
                        for (int i = 0; i < stacks.length; i++)
                            tabInventory.setInventorySlotContents(i, stacks[i], entry.getKey());
                    }
                } else {
                    System.err.println("В контейнере " + container.toString() + " не найдет TabInventory с именем " + message.inventoryName);
                }
                //}
            }

            // Пересчитывает параметры игрока на клинте
            player.updateParams();

            return null;
        }
    }
}
