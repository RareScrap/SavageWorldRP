package rsstats.inventory.container;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import rsstats.common.CommonProxy;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.items.MiscItems;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Minecraft.class, Entity.class, ExtendedPlayer.class, SimpleNetworkWrapper.class}) // TODO: Разобраться и убрать ненужные классы
@PowerMockIgnore("javax.management.*")
@SuppressStaticInitializationFor("rsstats.common.CommonProxy") // Разобраться
public class TestMainContainer {
    EntityPlayer player;
    ExtendedPlayer extendedPlayer;
    StatsInventory statsInventory;
    SkillsInventory skillsInventory;
    InventoryPlayer inventoryPlayer;
    MainContainer mainContainer;

    @Before
    public void setup() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        player = PowerMockito.mock(EntityPlayerMP.class); // Мокаем игрока // TODO: Мокать ращличного игрока в зависимости от стороны
        player.worldObj = PowerMockito.mock(World.class);
        when(player.getEntityData()).thenReturn(new NBTTagCompound()); // Пустой компаунд даты игрока
        inventoryPlayer = new InventoryPlayer(player); // Используем мок
        player.inventory = inventoryPlayer;

        // TODO: Из-за того что конструктор приватный, ExtendedPlayer плохо расположен к тестированию
        Constructor c = ExtendedPlayer.class.getDeclaredConstructor(EntityPlayer.class);
        c.setAccessible(true);
        ExtendedPlayer extendedPlayer = (ExtendedPlayer) c.newInstance(player);
        PowerMockito.when(ExtendedPlayer.get(player)).thenReturn(extendedPlayer);
        PowerMockito.when(player.getAttributeMap()).thenReturn(new ServersideAttributeMap());
        extendedPlayer.init(player, player.worldObj); // Начальные статы раскидываются тут

        Minecraft mc = PowerMockito.mock(Minecraft.class); // Мокаем класс игры
        PowerMockito.mockStatic(Minecraft.class);
        PowerMockito.when(Minecraft.getMinecraft()).thenReturn(mc);

        PowerMockito.mockStatic(SimpleNetworkWrapper.class); // Мокаем сеть
        CommonProxy.INSTANCE = PowerMockito.mock(SimpleNetworkWrapper.class); // TODO CommonProxy.INSTANCE должна быть final. Нужно восстановить final, но не сломать тесты

        TabHostInventory tabHostInventory = new TabHostInventory("TabHostInventor", 9);
        TabInventory tabInventory = new TabInventory("TabInventory", 27, player, tabHostInventory);

        mainContainer = new MainContainer(extendedPlayer);
        player.inventory.mainInventory[0] = new ItemStack(MiscItems.expItem, 64); // Начальный очки прокачки
    }

    @Test
    public void TestStatUpAndDown() {
        // Прокачиваем стату от нуля до максимума
        mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        assertEquals(4, mainContainer.getSlot(9).getStack().getItemDamage());
        // И немного сверх нормы
        for (int i = 0; i < 100; i++) {
            mainContainer.processStatRightClick(mainContainer.getSlot(9), 0, player);
        }
        assertEquals(4, mainContainer.getSlot(9).getStack().getItemDamage());

        // Прокачиваем стату с максимума до нуля
        mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        assertEquals(0, mainContainer.getSlot(9).getStack().getItemDamage());
        // И немного сверх нормы
        for (int i = 0; i < 100; i++) {
            mainContainer.processStatMiddleClick(mainContainer.getSlot(9), 0, player);
        }
        assertEquals(0, mainContainer.getSlot(9).getStack().getItemDamage());
    }

    /**
     * Прокачивает стату на 3, а затем навык на 3, зависящий от этой статы. После уменьшает стату до 1, а потом
     * навык до 0.
     *
     * Смысл теста в том, чтобы разница потраченных и возвращенных очков прокачки равнялась 0. Это гарантирует
     * отсустве абуза.
     */
    @Test
    public void Test_StatUp3_skillUp3_StatDown3_SkillDown3() {
        player.worldObj.isRemote = false; // Серверный режим включен

        // Начинаем с 20 очками прокачки
        ItemStack points = new ItemStack(MiscItems.expItem, 20);
        inventoryPlayer.setInventorySlotContents(0, points); // TODO по непонятной причине addItemStackToInventory не работает
        mainContainer.getSkillsInventory().setCurrentTab("item.StrengthStatItem");

        mainContainer.slotClick(9, 1, 0, player); // Прокачиваем стату
        mainContainer.slotClick(9, 1, 0, player);
        mainContainer.slotClick(9, 1, 0, player);
        mainContainer.slotClick(18, 1, 0, player); // Прокачиваем навык
        mainContainer.slotClick(18, 1, 0, player);
        mainContainer.slotClick(18, 1, 0, player);
        mainContainer.slotClick(9, 2, 0, player); // Уменьшаем стату
        mainContainer.slotClick(9, 2, 0, player);
        mainContainer.slotClick(9, 2, 0, player);
        mainContainer.slotClick(18, 2, 0, player); // Уменьшаем навык
        mainContainer.slotClick(18, 2, 0, player);
        mainContainer.slotClick(18, 2, 0, player);

        assertEquals(20, points.stackSize);
    }

    // TODO: Невозможно запустить, т.к. нет публичного метода для возвращения стоимости прокачки
//    @Test
//    public void TestServerSlotClick() {
//        player.worldObj.isRemote = false; // Серверный режим включен
//
//        // ВНИМАНИЕ! Перед тестом убирай final с CommonProxy#INSTANCE
//        // Тестируем клик на слот с двумя очками прокачки в инвентаре. В итоге ...
//        inventoryPlayer.addItemStackToInventory(new ItemStack(new ExpItem("ExpItem"), 2));
//        int upgradePrice = extendedPlayer.levelupManager.getUpgradePrice(statsInventory.getStackInSlot(0));
//        mainContainer.slotClick(9, 1, 0, player);
//        assertEquals(2, upgradePrice); // ... вычисляется цена прокачки
//        assertEquals(1, mainContainer.getSlot(9).getStack().getItemDamage()); // ... стата прокачивается
//        for (int i = 0; i < inventoryPlayer.getSizeInventory(); i++) {
//            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
//            if (itemStack != null) {
//                fail(); // ... в инвентаре игрока не остается очков прокачки
//            }
//        }
//    }
}
