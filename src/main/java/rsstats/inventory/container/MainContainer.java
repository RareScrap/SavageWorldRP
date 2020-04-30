package rsstats.inventory.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.api.items.perk.PerkItem;
import rsstats.common.CommonProxy;
import rsstats.common.network.PacketContainerChange;
import rsstats.common.network.PacketContainerContent;
import rsstats.data.ExtendedPlayer;
import rsstats.data.LevelupManager;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.slots.SkillSlot;
import rsstats.inventory.slots.StatSlot;
import rsstats.items.MiscItems;
import rsstats.items.OtherItems;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import ru.rarescrap.tabinventory.TabContainer;
import ru.rarescrap.tabinventory.TabInventory;

/**
 *
 * @author rares
 */
public class MainContainer extends TabContainer {
    private final ExtendedPlayer player;

    /** True, если игрок начал прокачивать статы, перейдя тем самым в режим редактирования */
    public boolean isEditMode = false;

    public MainContainer(ExtendedPlayer player) {
        this.player = player;

        // Добавляем вкладочный инвентарь к контейнеру
        tabInventories.put(player.skillsInventory.getInventoryName(), player.skillsInventory);
        tabInventories.put(player.otherTabsInventory.getInventoryName(), player.otherTabsInventory);
        // Добавляем его к движку синхронизации (СЕРВЕР->КЛИЕНТ)
        if (player.isServerSide()) {
            getSync().addSync(player.skillsInventory);
            getSync().addSync(player.otherTabsInventory);
        }

        addSlots();
    }
    
    private void addSlots() {
        /*if (inventoryPlayer != null)
            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    this.addSlotToContainer(new Slot(inventoryPlayer, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
                }
            }*/

        // Расставляем слоты на панели руки
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(player.getEntityPlayer().inventory, i, (i*18 -3) +8, 188));
        }

        // Расставляем слоты на панели статов
        for (int i = 0, slotIndex = 0; i < player.statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(player.statsInventory, i, (i*18 +167) +8, /*-24*/8));
            //this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Расставляем слоты на панели скиллов
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new SkillSlot(player.skillsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 26));
            }
        }

        // Расставляем слоты для брони
        for (int i = 0; i < 4; ++i) {
            final int k = i;
            this.addSlotToContainer(new Slot(player.getEntityPlayer().inventory, player.getEntityPlayer().inventory.getSizeInventory() - 1 - i, (i * 18 + 51) + 8, 8) {

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack par1ItemStack) {
                    if (par1ItemStack == null) return false;
                    return par1ItemStack.getItem().isValidArmor(par1ItemStack, k, player.getEntityPlayer());
                }

                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex() {
                    return ItemArmor.func_94602_b(k);
                }


                @Override
                public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack itemStack) {
                    super.onPickupFromSlot(p_82870_1_, itemStack);
                    player.modifierManager.removeModifiersFrom(itemStack); // Удаляем модификаторы от прошлой брони
                }

                /**
                 * Helper method to put a stack in the slot.
                 *
                 * @param itemStack
                 */
                @Override
                public void putStack(ItemStack itemStack) {
                    // TODO: Баг с рассинхронизацией модификаторов на клиенте и серве был впервые замечен тут
                    // if (!player.worldObj.isRemote) - не работает на клиенте
                    if (!player.isServerSide()) {
                        // Если в слоте уже был предмет - удаляем его модификаторы
                        if (this.getStack() != null) {
                            player.modifierManager.removeModifiersFrom(this.getStack());
                        }
                        // Извлекаем и сохраняем модификаторы из стака, который кладется в слот
                        player.modifierManager.addModifiersFrom(itemStack);
                    }
                    super.putStack(itemStack);

                    // Дебажная инфа
                    /*if (player.worldObj.isRemote) {
                        System.out.println("Клиентские модификаторы:");
                    } else {
                        System.out.println("Серверные модификаторы:");
                    }
                    for (ArrayList<RollModifier> modifiers : ExtendedPlayer.get(player).getModifierMap().values()) {
                        for (RollModifier modifier : modifiers) {
                            System.out.println(modifier);
                        }

                    }*/
                }
            });
        }

        // Расставляем слоты на панели носимых вещей
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                this.addSlotToContainer(new Slot(player.wearableInventory, x + y * 4 /*+ 9*/, (x*18 + 51) +8, (y * 18) + 26) {
                    // Сюда нельзя помещать броню
                    @Override
                    public boolean isItemValid(ItemStack p_75214_1_) {
                        if (p_75214_1_.getItem() instanceof ItemArmor)
                            return false;
                        else
                            return super.isItemValid(p_75214_1_);
                    }
                });
            }
        }

        // Расставляем слоты на панели вкладок
        for (int i = 0, slotIndex = 0; i < player.otherTabsHost.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new Slot(player.otherTabsHost, i, (i*18 +167) +8, 116) {
                @Override
                public boolean isItemValid(ItemStack p_75214_1_) {
                    return player.otherTabsHost.isUseableByPlayer(player.getEntityPlayer());
                }
            });

        }

        // Расставляем слоты, которе будут хранить содержимое вкладок
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(player.otherTabsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 134) {
                    @Override
                    public boolean isItemValid(ItemStack itemStack) {
                        // Получаем имя вкладки с перками
                        String perkTabName = OtherItems.perksTabItem.getUnlocalizedName();

                        // Проверяем в какую вкладку игрок хочет поместить стак (пока проверяем только очет ли он поместить ее в вкладку перков)
                        if (itemStack != null && itemStack.getItem() instanceof PerkItem && player.otherTabsInventory.getCurrentTab().equals(perkTabName)) {
                            // Проверяем, может ли игрок использовать целевой инвентарь и удовлетворяет ли игрок требованиям перка
                            PerkItem perkItem = (PerkItem) itemStack.getItem();
                            return player.otherTabsHost.isUseableByPlayer(player.getEntityPlayer()) && perkItem.isSuitableFor(player);
                        }

                        return false;

                        /* Небользая заметка: раз этот метод исполняется на клиенте, то результат работы этого метода
                         * должен бть одинаков на обоих сторонах. Возможно, это первый звоночек к тому, что придется
                         * держать постоянную (т.е. не в рамках срока жизни одного контейнера) ВСЕЙ инфы ExtendedPlayer'а
                         * с клиентом. Сделать это можно так же как ванильный код поддерживает синхронизацию
                         * EntityPlayer#inventoryContainer. */
                    }
                });
            }
        }
    }

    /**
     * This should always return true, since custom inventory can be accessed from anywhere
     * @param player TODO
     * @return TODO
     */
    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
    
    /**
     * Called when a entityPlayer shift-clicks on a slot. You must override this or you will crash when someone does that.
     * Basically the same as every other container I make, since I define the same constant indices for all of them
     * @param player TODO
     * @param par2 TODO
     * @return TODO
     */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(par2);
        return itemstack;
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        if (!crafters.contains(p_75132_1_)) {

            if (p_75132_1_ instanceof EntityPlayerMP) {
                // Высылаем содержимое слотов контейнера игроку на клиент
                // TODO: Почему тут нельзя положится на уже имеющуюся логику ванилы и TabInventoryLib?
                CommonProxy.INSTANCE.sendTo(new PacketContainerContent(this), (EntityPlayerMP) p_75132_1_);
                crafters.add(p_75132_1_);
            } else {
                // TODO: Понятия не имею как разруливать ситуацию, если это случится
                super.addCraftingToCrafters(p_75132_1_);
            }

            detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges() {
        /* Т.к. по логике данного контейнера slotClick(...) может не работать на клиенте и сервере одинакого,
         * то для поддержания работоспособности синхронизации нам нужно выставить isChangingQuantityOnly = false
         * см. NetHandlerPlayServer#processClickWindow().
         *
         * Кейс: Если этого не сделать, то при прокачке статы на клиент не будет высылаться пакет об уменьшении
         * количества очков прокачки. По логике данного контейнера, проверку на возможность прокачки навыка/статы
         * осуществляет сервер. Именно поэтому slotClick(...) работает по разному на клиенте и сервере.
         */
        // https://rarescrap.blogspot.com/2018/10/minecraft-1_18.html?zx=7ca4a4ed658beb3
        if (player.getEntityPlayer() instanceof EntityPlayerMP)
            ((EntityPlayerMP) player.getEntityPlayer()).isChangingQuantityOnly = false; // TODO: Кандидат на удаление, т.к. ванильная синзронизация из EntityPlayerMP более не используется

        // Вызов на клиенте ни к чему не приведет, т.к. список crafters будет пустым // TODO: А будет ли вызов на клиенте?
        // copy-paste from TabContainer#detectAndSendChanges()
        for (int i = 0; i < inventorySlots.size(); ++i)
        {
            /* ================================== MinecraftTabInventory START ================================== */
            // Пропускаем синхронизацию слотов из TabInventory. Этой задачей займется другой объект.
            if ( ((Slot) inventorySlots.get(i)).inventory instanceof TabInventory ) {
                continue;
            }
            /* =================================== MinecraftTabInventory END =================================== */

            ItemStack itemstack = ((Slot)inventorySlots.get(i)).getStack();
            ItemStack itemstack1 = (ItemStack)inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
            {
                itemstack1 = itemstack == null ? null : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < crafters.size(); ++j)
                {
                    if (crafters.get(j) instanceof EntityPlayerMP) {
                        // Высылаем изменение в слоте (Только для обычных инвентарей, не для TabInventory)
                        CommonProxy.INSTANCE.sendTo(new PacketContainerChange(itemstack1, i), (EntityPlayerMP) crafters.get(j));
                    } else {
                        // TODO: Понятия не имею как разруливать ситуацию, если это случится
                        ((ICrafting) crafters.get(j)).sendSlotContents(this, i, itemstack1);
                    }
                }
            }
        }

        getSync().detectAndSendChanges(crafters); // Синхронизируем TabInventory'и
    }

    // TODO: Это выполняется и для клиента и для сервера. Разгранич код. Приводит ли такое поведение к рассинхронизации?
    @Override
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        // -999 - "переносимый" стак кликается за предеты контейнера (т.е. выбрасывается)
        // -1 - игрок тыкает переносимым стаков в то место в контейнере, в котором нет слота
        if (slotId == -999 || slotId == -1)
            return super.slotClick(slotId, clickedButton, mode, playerIn);

        Slot slot = getSlot(slotId);
        Item itemInSlot;
        if (slot.getStack() != null) {
            itemInSlot = slot.getStack().getItem();
        } else {
            return super.slotClick(slotId, clickedButton, mode, playerIn);
        }

        if (clickedButton == 1 && itemInSlot instanceof StatItem) { // ПКМ
            ItemStack temp = slot.getStack().copy();
            processStatRightClick(slot, mode, playerIn);

            /* Не следует возвращать прокачанный ItemStack, т.к. тогда
             * NetHandlerPlayServer#processClickWindow() обнаружит что стак, по которому кликнул игрок
             * не равен стаку в серверном инвентаре по такой же позиции. Это приведет к тому, что
             * ВСЕ содержимое окна перешлется на клиент, что не очень эффективно. */
            return temp;
        }

        if (clickedButton == 2 && itemInSlot instanceof StatItem) { // СКМ
            ItemStack temp = slot.getStack().copy();
            processStatMiddleClick(slot, mode, playerIn);
            return temp;
        }

        if ((slot.inventory == player.statsInventory || slot.inventory == player.skillsInventory) && (itemInSlot instanceof SkillItem || itemInSlot instanceof StatItem)) {
            ItemStack itemStack = getSlot(slotId).getStack();

            // Защита от дублирующихся сообщений в чате + ролл посылается в клиента, где определен класс GuiScreen
            if (playerIn.worldObj.isRemote) {
                ( (StatItem) itemStack.getItem() ).sendRollPacket(itemStack, playerIn, !GuiScreen.isCtrlKeyDown());
            }
            return slot.getStack();
        }

        // Поведение, если кликнут слот инвентаря otherTabsHost
        if (slot.inventory == player.otherTabsHost) {
            if (player.otherTabsHost.isUseableByPlayer(playerIn)) { // TODO: Не лучше ли использовать isItemValid из переопределенного слота?
                return super.slotClick(slotId, clickedButton, mode, playerIn); // "Захватваем" стак
            } else {
                return null; // Ничего не делаем
            }
        }
        // Поведение, если кликнут слот инвентаря otherTabsInventory
        if (slot.inventory == player.otherTabsInventory) {
            if (player.otherTabsInventory.isUseableByPlayer(playerIn)) {
                return super.slotClick(slotId, clickedButton, mode, playerIn);
            } else if (player.otherTabsInventory.getCurrentTab().equals(OtherItems.perksTabItem.getUnlocalizedName())) {
                PerkItem perkItem = (PerkItem) slot.getStack().getItem();
                if (perkItem.canActivate() && !player.cooldownManager.isCooldown(perkItem)) perkItem.activate(player);
                return null;
            }
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    // Пересчет при закрытии контейнера нужен, когда в диалоге нажимается "Отменить изменения"
    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        isEditMode = false;
        super.onContainerClosed(entityPlayer);
    }

    public SkillsInventory getSkillsInventory() {
        return player.skillsInventory;
    }

    /**
     * Обрабатывает ПКМ по стате/навыку, т.е. намерение пользователя прокачать навык
     * @param slot Слот, в котором лежит стата/навык
     * @param mode Режим клик (см. {@link net.minecraft.client.gui.inventory.GuiContainer#handleMouseClick})
     * @param playerIn Игрок, нажавший ПКМ
     * @return Стак, по которому был сделан клик
     */
    protected ItemStack processStatRightClick(Slot slot, int mode, EntityPlayer playerIn) {
        // Если у игрока есть очки прокачки и он не в режиме редактирования ...
        if (playerIn.inventory.hasItem(MiscItems.expItem) && !isEditMode) {
            // ... тогда инициализируем режим прокачки и сохраняем текущий билд игрока
            isEditMode = true;
            if (!playerIn.worldObj.isRemote) {
                player.levelupManager = new LevelupManager(player);
            } else {
                return slot.getStack();
            }
        }

        if (isEditMode && !playerIn.worldObj.isRemote) { // Игрок в режиме прокачки - пытается повысить стату/навык
            player.levelupManager.statUp(slot.getStack());
            ExtendedPlayer.get(playerIn).sync(); // Информируем клиент, чтобы он пересчитал параметры
        }

        return slot.getStack();
    }

    /**
     * Обрабатывает СКМ по стате/навыку, т.е. намерение пользователя отменить прокачку навыка
     * @param slot Слот, в котором лежит стата/навык
     * @param mode Режим клика (см. {@link net.minecraft.client.gui.inventory.GuiContainer#handleMouseClick})
     * @param playerIn Игрок, нажавший ЛКМ
     * @return Стак, по которому был сделан клик
     */
    protected ItemStack processStatMiddleClick(Slot slot, int mode, EntityPlayer playerIn) {
        if (playerIn.worldObj.isRemote) // Расчет производится только на сервере
            return slot.getStack();

        if (isEditMode) { // Игрок в режиме прокачки - пытается понизить стату/навык
            player.levelupManager.statDown(slot.getStack());
            ExtendedPlayer.get(playerIn).sync(); // Информируем клиент, чтобы он пересчитал параметры
        }
        return slot.getStack();
    }

}