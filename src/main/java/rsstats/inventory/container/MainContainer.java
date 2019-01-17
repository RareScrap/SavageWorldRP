package rsstats.inventory.container;

import cpw.mods.fml.common.registry.GameRegistry;
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
import rsstats.common.RSStats;
import rsstats.common.network.PacketContainerChange;
import rsstats.common.network.PacketContainerContent;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.inventory.slots.SkillSlot;
import rsstats.inventory.slots.StatSlot;
import rsstats.items.MiscItems;
import rsstats.items.OtherItems;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.items.perk.PerkItem;
import rsstats.utils.Utils;
import ru.rarescrap.tabinventory.TabContainer;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rares
 */
public class MainContainer extends TabContainer {
    private final EntityPlayer player;
    private final InventoryPlayer inventoryPlayer;
    private final StatsInventory statsInventory;
    private final WearableInventory wearableInventory;
    private final SkillsInventory skillsInventory;

    private final TabHostInventory otherTabsHost;
    private final TabInventory otherTabsInventory;

    /** True, если игрок начал прокачивать статы, перейдя тем самым в режим редактирования */
    public boolean isEditMode = false;

    /** Хранит в себе прокачку игрока, которая была до того как он начал раскидывать очки прокачки.
     * Используется для отката изменений. */
    /* Может показаться, что ключ уже хранит свой itemDamage, который является уровнем статы, и можно просто
     * реализовать хранение через ArrayList, но не нужно забывать, что itemDamage в ключах - может быть измен
     * игроком, что делает хранилище ArrayList, т.к. сохраненные уровни стат будут утеряны */
    private Map<ItemStack, Integer> savedBild = new HashMap<ItemStack, Integer>(); // TODO: Заменить пару на String-Integer
    /** Хранит историю трат при прокачке в формате стак->массив. Каждая ячейка массива соответствует цене,
     * которую заплатил игрок для поднятия уровня стат на 1. */
    protected Map<ItemStack, ArrayList<Integer>> upgradeHistory;

    /** Количество очков прокачки, которые следует вернуть игроку, если тот решил отменить прокачку */
    private int wastedPoints;

    public MainContainer(EntityPlayer player, InventoryPlayer inventoryPlayer, StatsInventory statsInventory, SkillsInventory skillsInventory, WearableInventory wearableInventory, TabHostInventory otherTabsHost, TabInventory otherTabsInventory) {
        this.player = player;
        this.inventoryPlayer = inventoryPlayer;
        this.statsInventory = statsInventory;
        this.skillsInventory = skillsInventory;
        this.wearableInventory = wearableInventory;

        this.otherTabsHost = otherTabsHost;
        this.otherTabsInventory = otherTabsInventory;

        // Добавляем вкладочный инвентарь к контейнеру
        tabInventories.put(skillsInventory.getInventoryName(), skillsInventory);
        tabInventories.put(otherTabsInventory.getInventoryName(), otherTabsInventory);
        // Добавляем его к движку синхронизации (СЕРВЕР->КЛИЕНТ)
        if (!player.worldObj.isRemote) {
            getSync().addSync(skillsInventory);
            getSync().addSync(otherTabsInventory);
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
            this.addSlotToContainer(new Slot(inventoryPlayer, i, (i*18 -3) +8, 188));
        }

        // Расставляем слоты на панели статов
        for (int i = 0, slotIndex = 0; i < statsInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new StatSlot(statsInventory, i, (i*18 +167) +8, /*-24*/8));
            //this.addSlotToContainer(new StatSlot(statsInventory, slotIndex, i*9, 0));
        }

        // Расставляем слоты на панели скиллов
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new SkillSlot(skillsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 26));
            }
        }

        // Расставляем слоты для брони
        for (int i = 0; i < 4; ++i) {
            final int k = i;
            this.addSlotToContainer(new Slot(inventoryPlayer, inventoryPlayer.getSizeInventory() - 1 - i, (i * 18 + 51) + 8, 8) {

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack par1ItemStack) {
                    if (par1ItemStack == null) return false;
                    return par1ItemStack.getItem().isValidArmor(par1ItemStack, k, player);
                }

                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex() {
                    return ItemArmor.func_94602_b(k);
                }


                @Override
                public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack itemStack) {
                    super.onPickupFromSlot(p_82870_1_, itemStack);
                    ExtendedPlayer.get(player).modifierManager.removeModifiersFrom(itemStack); // Удаляем модификаторы от прошлой брони
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
                    if (/*!*/player.worldObj.isRemote) {
                        // Если в слоте уже был предмет - удаляем его модификаторы
                        if (this.getStack() != null) {
                            ExtendedPlayer.get(player).modifierManager.removeModifiersFrom(this.getStack());
                        }
                        // Извлекаем и сохраняем модификаторы из стака, который кладется в слот
                        ExtendedPlayer.get(player).modifierManager.addModifiersFrom(itemStack);
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
                this.addSlotToContainer(new Slot(wearableInventory, x + y * 4 /*+ 9*/, (x*18 + 51) +8, (y * 18) + 26) {
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
        for (int i = 0, slotIndex = 0; i < otherTabsHost.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new Slot(otherTabsHost, i, (i*18 +167) +8, 116) {
                @Override
                public boolean isItemValid(ItemStack p_75214_1_) {
                    return otherTabsHost.isUseableByPlayer(player);
                }
            });

        }

        // Расставляем слоты, которе будут хранить содержимое вкладок
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(otherTabsInventory, x + y * 9 /*+ 9*/, (x*18 +167) +8, (y * 18) + 134) {
                    @Override
                    public boolean isItemValid(ItemStack itemStack) {
                        // Получаем имя вкладки с перками
                        String perkTabName = OtherItems.perksTabItem.getUnlocalizedName();

                        // Проверяем в какую вкладку игрок хочет поместить стак (пока проверяем только очет ли он поместить ее в вкладку перков)
                        if (itemStack != null && itemStack.getItem() instanceof PerkItem && otherTabsInventory.getCurrentTab().equals(perkTabName)) {
                            // Проверяем, может ли игрок использовать целевой инвентарь и удовлетворяет ли игрок требованиям перка
                            PerkItem perkItem = (PerkItem) itemStack.getItem();
                            return otherTabsHost.isUseableByPlayer(player) && perkItem.isSuitableFor(ExtendedPlayer.get(player));
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

    // TODO: баг при стаках очков прокачки 64 и 1
    // TODO: Стоимость прокачки статы должна быть 2 очка, а не 1
    /**
     * Увеличивает переданную стату на 1, если в {@link #inventoryPlayer} есть хотя бы один {@link rsstats.items.ExpItem}.
     * Так же уменьшает ExpItem на 1.
     * @param statStack Стак со статой, которую необходимо прокачать
     * @throws IllegalAccessException Если в statStack нет {@link StatItem}'а
     */
    public void statUp(ItemStack statStack) {
        if ( !(statStack.getItem() instanceof StatItem) ) {
            throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
        }

        StatItem statItem = (StatItem) statStack.getItem();

        // Находим стак с очками прокачки // TODO: Удаляем комментарии
//        ItemStack expStack = null;
//        int expStackPos = -1; // Если -1 - значит стак с очками прокачки нельзя создать + его и не было
//        for (int i = 0; i < inventoryPlayer.mainInventory.length; i++) {
//            ItemStack itemStack = inventoryPlayer.mainInventory[i];
//            if (itemStack != null && "item.ExpItem".equals(itemStack.getUnlocalizedName())) {
//                expStack = itemStack;
//                expStackPos = i;
//                break;
//            }
//        }
//        if (expStack == null) {
//            return; // Если очков прокачки нет - выходим
//        }

        // Выявляет количество подтипов (уровней) статы
//        List subitems = new ArrayList();
//        statItem.getSubItems(statItem, CreativeTabs.tabMaterials, subitems);
        int subitems = statItem.getMaxDamage();
        int statItemDamage = statItem.getDamage(statStack);


//        if (statItemDamage != subitems.size() - 1) {
//            int price = 1; // Цена прокачки
//            if (statItem instanceof SkillItem) {
//                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
//                int parentStatDamage = ((SkillItem) statItem).parentStat.getDamage(parentStatStack);
//                if (statItemDamage > parentStatDamage)
//                    price = 2;
//            }
//
//            // Отнимает очко прокачки ...
//            if (expStack.stackSize >= price) {
//                wastedPoints += price; // Сохраняем количество очков, что потратил пользователь
//                if (expStack.stackSize == price) {
//                    inventoryPlayer.mainInventory[expStackPos] = null; // Убираем стак
//                } else {
//                    expStack.stackSize -= price; // Уменьшаем стак
//                }
//            } else {
//                return;
//            }

        // и увеличиваем стату ...
        statStack.setItemDamage(statItemDamage < subitems ? statItemDamage + 1 : subitems);
//        } else { // Стата уже прокачана до предела - выходим
//            return;
//        }
    }

    // TODO: Рефакторить. См addItemStackToInventory
    public void statDown(ItemStack statStack) {
        if ( !(statStack.getItem() instanceof StatItem) ) {
            throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
        }

        StatItem statItem = (StatItem) statStack.getItem();
        int statItemDamage = statItem.getDamage(statStack);
//        boolean isExpStackCreated = false; // TODO: Удаляем комментарии

        /* В случае, если игрок в ходе одной сессии прокачки захотел обнулить
         * навыки, прокачанный в прошлой сесии - останавливаем его */
//        ItemStack s = statStack;
//        for (ItemStack keyStack : savedBild.keySet()) { // TODO: Нужно найти и использовать уже имеющийся поиск. Задолбало его писать каждый раз
//            if (keyStack.getUnlocalizedName().equals(statStack.getUnlocalizedName())) {
//                s = keyStack;
//                break;
//            }
//        }
//        if (statItemDamage <= savedBild.get(s)) {
//            return;
//        }

        // Находим стак с очками прокачки
//        ItemStack expStack = null;
//        for (ItemStack itemStack : inventoryPlayer.mainInventory) {
//            if (itemStack != null && "item.ExpItem".equals(itemStack.getUnlocalizedName())) {
//                expStack = itemStack;
//            }
//        }
//        // Если очков прокачки нет или их стак забит - ищем свободное место в инветаре, куда их можно положить
//        if (expStack == null || expStack.stackSize >= expStack.getMaxStackSize()) {
//            int freeSpaceIndex = findFreeSpaceInInventory(inventoryPlayer);
//            if (freeSpaceIndex != -1 && statItemDamage != 0) {
//                expStack = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"));
//                isExpStackCreated = true;
//                inventoryPlayer.setInventorySlotContents(freeSpaceIndex, expStack);
//            } else { // Если свободное место не было найдено - выходим
//                return;
//            }
//        }
//
//        // Выявляет количество подтипов (уровней) статы
//        List subitems = new ArrayList();
//        statItem.getSubItems(statItem, CreativeTabs.tabMaterials, subitems);
//
//        if (statItemDamage > 0) {
//            int reward; // Сколько очков прокачки вернется за отмену прокачки
//            if (statItem instanceof SkillItem) {
//                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
//                int parentStatDamage = ((SkillItem) statItem).parentStat.getDamage(parentStatStack);
//                if (statItemDamage > parentStatDamage+1)
//                    reward = 2;
//                else
//                    reward = 1;
//            } else { // instanceof StatItem
//                reward = 1;
//            }
//
//            // возвращаем игроку очки прокачки ...
//            if (expStack.stackSize + reward <= expStack.getMaxStackSize()) {
//                // Убираем очки из "возмещения", если тот сам (т.е. без диалога) отменил прокачку конкретного навыка или статы
//                wastedPoints -= reward;
//
//                if (isExpStackCreated) {
//                    expStack.stackSize = reward;
//                } else {
//                    expStack.stackSize += reward;
//                }
//            } else {
//                reward = (expStack.stackSize + reward) % expStack.getMaxStackSize();
//                expStack.stackSize = expStack.getMaxStackSize();
//
//                int freeSpaceIndex = findFreeSpaceInInventory(inventoryPlayer);
//                if (freeSpaceIndex != -1) {
//                    expStack = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"));
//                    expStack.stackSize = reward;
//                    inventoryPlayer.setInventorySlotContents(freeSpaceIndex, expStack);
//                } else {
//                    return;
//                }
//            }

        // и уменьшаем стату ...
        statStack.setItemDamage(statItemDamage > 0 ? statItemDamage-1 : 0);
//        } else { // Стата уже спущена до минимального предела - выходим
//            return;
//        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        if (!crafters.contains(p_75132_1_)) {
            crafters.add(p_75132_1_);

            if (p_75132_1_ instanceof EntityPlayerMP) {
                // Высылаем содержимое слотов контейнера игроку на клиент
                CommonProxy.INSTANCE.sendTo(new PacketContainerContent(this), (EntityPlayerMP) p_75132_1_);
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

        if ((slot.inventory == statsInventory || slot.inventory == skillsInventory) && (itemInSlot instanceof SkillItem || itemInSlot instanceof StatItem)) {
            ItemStack itemStack = getSlot(slotId).getStack();

            // Защита от дублирующихся сообщений в чате + ролл посылается в клиента, где определен класс GuiScreen
            if (playerIn.worldObj.isRemote) {
                ( (StatItem) itemStack.getItem() ).sendRollPacket(itemStack, playerIn, !GuiScreen.isCtrlKeyDown());
            }
            return slot.getStack();
        }

        // Поведение, если кликнут слот инвентаря otherTabsHost
        if (slot.inventory == otherTabsHost) {
            if (otherTabsHost.isUseableByPlayer(playerIn)) { // TODO: Не лучше ли использовать isItemValid из переопределенного слота?
                return super.slotClick(slotId, clickedButton, mode, playerIn); // "Захватваем" стак
            } else {
                return null; // Ничего не делаем
            }
        }
        // Поведение, если кликнут слот инвентаря otherTabsInventory
        if (slot.inventory == otherTabsInventory) {
            if (otherTabsInventory.isUseableByPlayer(playerIn)) {
                return super.slotClick(slotId, clickedButton, mode, playerIn);
            } else {
                return null;
            }
        }

        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    // Пересчет при закрытии контейнера нужен, когда в диалоге нажимается "Отменить изменения"
    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        // Пересчитываем параметры и синхронизируем их с клиентом
        if (!entityPlayer.worldObj.isRemote) {
            ExtendedPlayer.get(player).updateParams();
            ExtendedPlayer.get(player).sync();
        }
        super.onContainerClosed(entityPlayer);
    }

    public SkillsInventory getSkillsInventory() {
        return skillsInventory;
    }

    /**
     * Сохраняет прокачку персонажа, дабыы иметь возможность ее восстановить
     */
    public void saveBild() {
        savedBild.clear();
        for (ItemStack statStack : statsInventory.getStats()) {
            if (statStack != null)
                savedBild.put(statStack, statStack.getItemDamage());
        }
        for (ItemStack skillStack : skillsInventory.getSkills()) {
            if (skillStack != null)
                savedBild.put(skillStack, skillStack.getItemDamage());
        }
    }

    /**
     * Восстанавливает прокачку персонажа
     */
    public void restoreBild() {
        for (ItemStack bildStack : savedBild.keySet()) {
            int lvl = savedBild.get(bildStack);
            if (bildStack.getItem() instanceof SkillItem) {
                for (ItemStack currentSkillStack : skillsInventory.getSkills()) {
                    if (currentSkillStack != null && currentSkillStack.getItem() == bildStack.getItem()) {
                        currentSkillStack.setItemDamage(lvl);
                    }
                }
            } else {
                for (ItemStack currentStatStack : statsInventory.getStats()) {
                    if (currentStatStack != null && currentStatStack.getItem() == bildStack.getItem()) {
                        currentStatStack.setItemDamage(lvl);
                    }
                }
            }
        }

        /* addItemStackToInventory успешно работает с ситуацией, если вернутся больше чем 64 предмета.
         * Нет нужды в своих проверках. */
        ItemStack expStack = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"), wastedPoints);
        this.player.inventory.addItemStackToInventory(expStack);
    }

    /**
     * Вычисляет стоимость прокачки статы или навыка на один пункт.
     * @param statStack Стак со статой
     * @return Стоимость прокачки статы или навыка на один пункт. Если достигнут предел, вовзращает -1
     * @throws IllegalAccessException Если в statStack нет {@link StatItem}'а
     */
    public int getUpgradePrice(ItemStack statStack) {  // TODO: Unit-test this
        if ( !(statStack.getItem() instanceof StatItem) ) {
            throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
        }

        StatItem statItem = (StatItem) statStack.getItem();

//        List subitems = new ArrayList(); // TODO: Удалить закоментированный код
//        statStack.getItem().getSubItems(statItem, CreativeTabs.tabMaterials, subitems); // TODO: БАГ! Крашится на Dedicated сервере. Заменить костыль ниже на приемлимый аналог
        int subtypes = statItem.getMaxDamage();
//        if (statItem instanceof SkillItem) {
//            subtypes = SkillItem.NUMBER_OF_LEVELS - 1;
//        } else { // statItem instanceof StatItem
//            subtypes = StatItem.NUMBER_OF_LEVELS - 1;
//        }

        int price = 1; // Цена прокачки по-умолчанию

        int statItemDamage = statItem.getDamage(statStack);
        if (statItemDamage != subtypes) {
            if (statItem instanceof SkillItem) {
                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
                int parentStatDamage = parentStatStack.getItemDamage(); //((SkillItem) statItem).parentStat.getDamage(parentStatStack);
                if (statItemDamage > parentStatDamage)
                    price = 2;
            } else { // instanceof StatItem ONLY
                price = 2;
            }

            return price;
        }

        return -1; // TODO: Не самый удачный выбор возвращаемого числа. Может быть сменить на 0?
    }

    /**
     * Вычисляет количество очков, которое получит игрок после даунгрейда статы или навыка на один пункт.
     * @param statStack Стак со статой
     * @return Возврат за даунгрейд статы или навыка на один пункт. Если достигнут предел, вовзращает -1
     * @throws IllegalAccessException Если в statStack нет {@link StatItem}'а
     */
    public int getDowngradeReward(ItemStack statStack) {  // TODO: Unit-test this
        if ( !(statStack.getItem() instanceof StatItem) ) {
            throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
        }

        StatItem statItem = (StatItem) statStack.getItem();
        int statItemDamage = statStack.getItemDamage();

        // Выявляет количество подтипов (уровней) статы // TODO: Удалить закоментированный код
//        List subitems = new ArrayList();
//        statItem.getSubItems(statItem, CreativeTabs.tabMaterials, subitems); // TODO: Вклалдка не нужна
        int subitem = statItem.getMaxDamage();

//        int reward; // Сколько очков прокачки вернется за отмену прокачки
//        if (statItemDamage > 0) {
//            if (statItem instanceof SkillItem) {
//                ItemStack parentStatStack = statsInventory.getStat(((SkillItem) statItem).parentStat.getUnlocalizedName());
//                int parentStatDamage = ((SkillItem) statItem).parentStat.getDamage(parentStatStack);
//                if (statItemDamage > parentStatDamage + 1)
//                    reward = 2;
//                else
//                    reward = 1;
//            } else { // instanceof StatItem ONLY
//                reward = 2;
//            }
//            return reward;
//        }

        //return -1;

        return getPriceFor(statStack, statStack.getItemDamage()/*-1*/);
    }

    /**
     * Определяет, может ли игрок получить возврат очков прокачки при попытки понизить стату
     * @param statStack Стак со статой
     * @return True, если может, false - нет или если попытается сбросить стату, которую не прокачивал в рамках текущей сессии прокачки.
     * @see #saveBild()
     */
    public boolean canRefund(ItemStack statStack) {  // TODO: Unit-test this
        if ( !(statStack.getItem() instanceof StatItem) ) {
            throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
        }

        int statItemDamage = statStack.getItemDamage();

        ItemStack s = statStack;
        for (ItemStack keyStack : savedBild.keySet()) { // TODO: Нужно найти и использовать уже имеющийся поиск. Задолбало его писать каждый раз
            if (keyStack.getUnlocalizedName().equals(statStack.getUnlocalizedName())) {
                s = keyStack;
                break;
            }
        }
        return statItemDamage > savedBild.get(s); // TODO: Проверка на null
    }

    /**
     * Возвращает игроку указанное количество очков прокачи и отнимает из их {@link #wastedPoints}
     * @param refund Очки прокачки, которые будут возвращены игроку
     */
    public void doRefund(int refund) { // TODO: Unit-test this
        ItemStack expStack = new ItemStack(MiscItems.expItem, refund);
        this.player.inventory.addItemStackToInventory(expStack);
        wastedPoints -= refund;
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
                wastedPoints = 0;
                saveBild();
                initUpgradeHistory();
            } else {
                return slot.getStack();
            }
        }

        if (isEditMode && !playerIn.worldObj.isRemote) { // Игрок в режиме прокачки - пытается повысить стату/навык
            int price = getUpgradePrice(slot.getStack());
            if (price != -1 && Utils.removeItemStackFromInventory(inventoryPlayer, "item.ExpItem", price)) {
                wastedPoints += price;

                // Добавляем трату очков в историю
                rememberPriceToNextLevel(slot.getStack(), price);

                // Поднимаем стату
                statUp(slot.getStack());

                // Пересчитваем параметры на сервере и информируем клиент, чтобы он сделал то же самое
                ExtendedPlayer extendedPlayer = ExtendedPlayer.get(playerIn);
                extendedPlayer.updateParams();
                extendedPlayer.sync();
            }
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

        int refund = getDowngradeReward(slot.getStack());
        if (isEditMode && canRefund(slot.getStack()) && refund != -1) { // Игрок в режиме прокачки - пытается понизить стату/навык
            statDown(slot.getStack());
            doRefund(refund);

            // Пересчитваем параметры на сервере и информируем клиент, чтобы он сделал то же самое
            ExtendedPlayer extendedPlayer = ExtendedPlayer.get(playerIn);
            extendedPlayer.updateParams();
            extendedPlayer.sync();
        }
        return slot.getStack();
    }

    /**
     * Инициализирует {@link #upgradeHistory} нулевой историей трат
     */
    protected void initUpgradeHistory() { // TODO: Unit-test this
        upgradeHistory = new HashMap<ItemStack, ArrayList<Integer>>();

        // Инициализируем историю трат для статов
        for (ItemStack itemStack : statsInventory.getStats()) {
            if (itemStack == null) continue;
            StatItem statItem = (StatItem) itemStack.getItem();

            ArrayList<Integer> history = new ArrayList<Integer>(
                    Collections.nCopies(statItem.getMaxDamage()+1, 0)
            );
            upgradeHistory.put(itemStack, history);
        }

        // Инициализируем историю трат для скиллов
        for (ItemStack itemStack : skillsInventory.getSkills()) {
            if (itemStack == null) continue;
            SkillItem skillItem = (SkillItem) itemStack.getItem();

            ArrayList<Integer> history = new ArrayList<Integer>(
                    Collections.nCopies(skillItem.getMaxDamage()+1, 0)
            );
            upgradeHistory.put(itemStack, history);
        }
    }

    /**
     * Сохраняет стомость прокачки, которую заплатит игрок, чтобы увеличить стату/навык на следующий уровень
     * @param itemStack Стак со статой/навыком
     * @param price Цена прокачки на следующий уровень
     */
    protected void rememberPriceToNextLevel(ItemStack itemStack, int price) { // TODO: Unit-test this
        ArrayList<Integer> history = upgradeHistory.get(itemStack);
        history.set(itemStack.getItemDamage()+1, price);
    }

    /**
     * Возвращет цену, которую заплатил игрок, чтобы стата перешла на определенный уровень (lvl) с предыдущего
     * @param itemStack Стак со статой/скиллом
     * @param lvl Уровень, на который поднялась стата с предыдущего уровня
     * @return Цена, которую заплатил игрок, чтобы стата перешла на определеннх уровень с предыдущего
     */
    protected int getPriceFor(ItemStack itemStack, int lvl) { // TODO: Unit-test this
        return upgradeHistory.get(itemStack).get(lvl);
    }

}