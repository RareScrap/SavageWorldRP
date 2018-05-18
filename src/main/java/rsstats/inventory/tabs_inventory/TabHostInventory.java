package rsstats.inventory.tabs_inventory;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;

/**
 * Инвентарь, хранящий вкладки. Каждый предмет в инвентаре является ключом, по корому устанавливается содержимое
 * в связанном {@link TabInventory}.
 */
public class TabHostInventory extends InventoryBasic {
    private static final String NBT_TAG = "player_data_tabs"; // TODO: Отказаться в пользу inventoryName
    private static boolean isHandlerRegistered = false;

    /** Имя последней выбранной вкладки */
    private String currentTab;
    /** Инвентарь, которые устанавливает свое наполнение в зависимости от {@link #currentTab}*/
    private TabInventory tabInventory;

    /**
     * Конструктор
     * @param inventoryName Имя инвентаря-хоста вкладок
     * @param inventorySize Размер инвентаря-хоста
     * @param tabInventory Связанный инвентарь для отображения содержимого выбранной вкладки
     */
    public TabHostInventory(String inventoryName, int inventorySize, TabInventory tabInventory) {
        super(NBT_TAG, false, inventorySize);
        this.tabInventory = tabInventory;
        this.func_110133_a(inventoryName); // Устанавливаем имя инвентаря
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        if (itemStack != null) {
            tabInventory.addTab(itemStack.getUnlocalizedName());
            super.setInventorySlotContents(slotIndex, itemStack);
        }
    }

    /* Этот метод не вызывается автоматически. Мы сами должны вызывать его где нам нужно.
     * Например, из кода контейнера или другого инвентаря. */
    // Оставлен для справки и в целях обучения
    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return entityPlayer.capabilities.isCreativeMode;
    }

    /* Этот метод не вызывается автоматически. Мы сами должны вызывать его где нам нужно.
     * Например, из кода контейнера или другого инвентаря. */
    // Оставлен для справки и в целях обучения
    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        return super.isItemValidForSlot(slotIndex, itemStack);
    }

    // TODO: нужно ли переопределять makeDirty()?

    public boolean isEmpty() {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }

        // We're storing our items in a custom tag list using our 'NBT_TAG' from above
        // to prevent potential conflicts
        compound.setTag(NBT_TAG, items);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        // Штатное чтение из NBT
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }
    }

    // Тут нет setTabInventory(), т.к. связанный инвентарь не должен меняться

    /**
     * Регистрирует обработчик для сообщений, поступающих из {@link TabHostInventory}.
     *
     * <p>
     * <strong>ВНИМАНИЕ!</strong>
     * Вы обязаны вызвать этот метод для preInit-фазы вашего мода. Иначе TabHostInventory и {@link TabInventory} не
     * смогут работать сообщая. Имейте ввиду, что для ВСЕХ TabHostInventory вашего мода регистрируется ЛИШЬ ОДИН
     * обработчик. Таким образом в должны позаботиться, чтобы связать нужный TabHostInventory с нужным {@link TabInventory}.
     * </p>
     *
     * @param handler Обработчик
     * @param discriminator Дискриминатор ID (ДОЛЖЕН БЫТЬ УНИКАЛЕН ДЛЯ ВСЕХ ОСТАЛЬНЫХ ПАКЕТОВ ВАШЕГО МОДА)
     * @see "https://mcforge.readthedocs.io/en/latest/networking/simpleimpl/#registering-packets"
     */
    public static void registerHandler(Class<? extends IMessageHandler<SetCurrentTabPacket, IMessage>> handler, int discriminator) {
        if (isHandlerRegistered) {
            throw new RuntimeException("Handler is already registered!");
        } else {
            CommonProxy.INSTANCE.registerMessage(handler, SetCurrentTabPacket.class, discriminator, Side.SERVER); // TODO: Передавать INSTANCE как параметр
            isHandlerRegistered = true;
        }
    }

    /**
     * Устанавливает текущую вкладку, отсылая сообщение обработчику, где в самостоятельно должн обработать сообщение.
     * @param tabName Имя вкладки
     */
    public void setTab(String tabName) {
        if (isHandlerRegistered) {
            if (!tabName.equals(currentTab)) { // Не отсылаем вообщение, если пытаемся установать вкладку, которая уже установлена
                CommonProxy.INSTANCE.sendToServer( // TODO: Сохранять INSTANCE, полученный в registerHandler()
                        new SetCurrentTabPacket(
                                this.getInventoryName(),
                                tabInventory.getInventoryName(),
                                tabName)
                );
                currentTab = tabName;
            }
        } else {
            throw new RuntimeException("Handler is not registered.");
        }
    }

    /**
     * Сообщение, отсылаемое обработчику при вызове {@link #setTab(String)}.
     */
    public static class SetCurrentTabPacket implements IMessage {
        /** Имя инвентарая-хоста вкладок, который отсылает сообщение */
        String callerInventoryName;
        /** Имя инвентаря {@link TabInventory}, который должен получить сообщение */
        String targetInventoryName;
        /** Имя вкладки, которую должен выставить {@link TabInventory} с именем {@link #targetInventoryName} */
        String newCurrentTabName;

        /**
         * Необходимый конструктор по умолчанию. Он необходим для того, чтобы на
         * стороне-обработчике создать объект и распаковать в него буффер.
         */
        public SetCurrentTabPacket() {
        }

        /**
         * Конструктор
         * @param callerInventoryName {@link #callerInventoryName}
         * @param targetInventoryName {@link #targetInventoryName}
         * @param newCurrentTabName {@link #newCurrentTabName}
         */
        public SetCurrentTabPacket(String callerInventoryName, String targetInventoryName, String newCurrentTabName) {
            this.callerInventoryName = callerInventoryName;
            this.targetInventoryName = targetInventoryName;
            this.newCurrentTabName = newCurrentTabName;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            callerInventoryName = ByteBufUtils.readUTF8String(buf);
            targetInventoryName = ByteBufUtils.readUTF8String(buf);
            newCurrentTabName = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, callerInventoryName);
            ByteBufUtils.writeUTF8String(buf, targetInventoryName);
            ByteBufUtils.writeUTF8String(buf, newCurrentTabName);
        }
    }
}
