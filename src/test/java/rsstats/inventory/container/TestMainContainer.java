package rsstats.inventory.container;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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
import rsstats.inventory.WearableInventory;
import rsstats.items.ExpItem;
import rsstats.items.MiscItems;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.roll.Roll;
import rsstats.roll.RollModifier;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Minecraft.class, Entity.class, ExtendedPlayer.class, SimpleNetworkWrapper.class}) // TODO: Разобраться и убрать ненужные классы
@PowerMockIgnore("javax.management.*")
@SuppressStaticInitializationFor("rsstats.common.CommonProxy") // Разобраться
public class TestMainContainer {
    EntityPlayer player;
    StatsInventory statsInventory;
    SkillsInventory skillsInventory;
    InventoryPlayer inventoryPlayer;
    MainContainer mainContainer;

    @Before
    public void setup() {
        player = PowerMockito.mock(EntityPlayerMP.class); // Мокаем игрока // TODO: Мокать ращличного игрока в зависимости от стороны
        player.worldObj = PowerMockito.mock(World.class);
        when(player.getEntityData()).thenReturn(new NBTTagCompound()); // Пустой компаунд даты игрока
        inventoryPlayer = new InventoryPlayer(player); // Используем мок
        player.inventory = inventoryPlayer;

        ExtendedPlayer extendedPlayer = PowerMockito.mock(ExtendedPlayer.class);
        PowerMockito.when(ExtendedPlayer.get(player)).thenReturn(extendedPlayer);

        Minecraft mc = PowerMockito.mock(Minecraft.class); // Мокаем класс игры
        PowerMockito.mockStatic(Minecraft.class);
        PowerMockito.when(Minecraft.getMinecraft()).thenReturn(mc);

        PowerMockito.mockStatic(SimpleNetworkWrapper.class); // Мокаем сеть
        CommonProxy.INSTANCE = PowerMockito.mock(SimpleNetworkWrapper.class); // TODO CommonProxy.INSTANCE должна быть final. Нужно восстановить final, но не сломать тесты

        initStatsAndSkills();

        TabHostInventory tabHostInventory = new TabHostInventory("TabHostInventor", 9);
        TabInventory tabInventory = new TabInventory("TabInventory", 27, player, tabHostInventory);
        mainContainer = new MainContainer(
                player,
                inventoryPlayer,
                statsInventory,
                skillsInventory,
                new WearableInventory(ExtendedPlayer.get(player)),
                tabHostInventory,
                tabInventory
        );
    }

    @Test
    public void TestStatUpAndDown() {

        // Прокачиваем стату от нуля до максимума
        mainContainer.statUp(statsInventory.getStackInSlot(0));
        mainContainer.statUp(statsInventory.getStackInSlot(0));
        mainContainer.statUp(statsInventory.getStackInSlot(0));
        mainContainer.statUp(statsInventory.getStackInSlot(0));
        mainContainer.statUp(statsInventory.getStackInSlot(0));
        assertEquals(4, mainContainer.getSlot(9).getStack().getItemDamage());
        // И немного сверх нормы
        for (int i = 0; i < 100; i++) {
            mainContainer.statUp(statsInventory.getStackInSlot(0));
        }
        assertEquals(4, mainContainer.getSlot(9).getStack().getItemDamage());

        // Прокачиваем стату с максимума до нуля
        mainContainer.statDown(statsInventory.getStackInSlot(0));
        mainContainer.statDown(statsInventory.getStackInSlot(0));
        mainContainer.statDown(statsInventory.getStackInSlot(0));
        mainContainer.statDown(statsInventory.getStackInSlot(0));
        mainContainer.statDown(statsInventory.getStackInSlot(0));
        assertEquals(0, mainContainer.getSlot(9).getStack().getItemDamage());
        // И немного сверх нормы
        for (int i = 0; i < 100; i++) {
            mainContainer.statDown(statsInventory.getStackInSlot(0));
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

    @Test
    public void TestServerSlotClick() {
        player.worldObj.isRemote = false; // Серверный режим включен

        // ВНИМАНИЕ! Перед тестом убирай final с CommonProxy#INSTANCE
        // Тестируем клик на слот с двумя очками прокачки в инвентаре. В итоге ...
        inventoryPlayer.addItemStackToInventory(new ItemStack(new ExpItem("ExpItem"), 2));
        int upgradePrice = mainContainer.getUpgradePrice(statsInventory.getStackInSlot(0));
        mainContainer.slotClick(9, 1, 0, player);
        assertEquals(2, upgradePrice); // ... вычисляется цена прокачки
        assertEquals(1, mainContainer.getSlot(9).getStack().getItemDamage()); // ... стата прокачивается
        for (int i = 0; i < inventoryPlayer.getSizeInventory(); i++) {
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (itemStack != null) {
                fail(); // ... в инвентаре игрока не остается очков прокачки
            }
        }
    }

    /**
     * Выставляет стандартный начальный билд
     */
    public void initStatsAndSkills() {
        statsInventory = new StatsInventory("stats", 9);
        skillsInventory = new SkillsInventory("skills", 27, player, statsInventory);

        ArrayList<Roll> statBasicRolls = new ArrayList<Roll>();
        statBasicRolls.add(new Roll(4));
        statBasicRolls.add(new Roll(6));
        statBasicRolls.add(new Roll(8));
        statBasicRolls.add(new Roll(10));
        statBasicRolls.add(new Roll(12));
        int i = 0;
        statsInventory.setInventorySlotContents(i++, new ItemStack(new StatItem(statBasicRolls, "StrengthStatItem", "rsstats:strenght", "item.StrengthStatItem")));
        statsInventory.setInventorySlotContents(i++, new ItemStack(new StatItem(statBasicRolls, "AgilityStatItem", "rsstats:agility", "item.AgilityStatItem")));
        statsInventory.setInventorySlotContents(i++, new ItemStack(new StatItem(statBasicRolls, "IntelligenceStatItem", "rsstats:intelligence", "item.IntelligenceStatItem")));
        statsInventory.setInventorySlotContents(i++, new ItemStack(new StatItem(statBasicRolls, "EnduranceStatItem", "rsstats:endurance", "item.EnduranceStatItem")));
        statsInventory.setInventorySlotContents(i++, new ItemStack(new StatItem(statBasicRolls, "CharacterStatItem", "rsstats:character", "item.CharacterStatItem")));


        RollModifier level_zero = new RollModifier(-2,"modifiers.MissingSkill");

        ArrayList<Roll> skillBasicRolls = new ArrayList<Roll>();
        skillBasicRolls.add(new Roll(4, level_zero)); // Создание дополнительного броска для нулевого уровня скиллов
        skillBasicRolls.add(new Roll(4));
        skillBasicRolls.add(new Roll(6));
        skillBasicRolls.add(new Roll(8));
        skillBasicRolls.add(new Roll(10));
        skillBasicRolls.add(new Roll(12));
        i = 0;
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "EquitationSkillItem", "rsstats:skills/Equitation", "item.EquitationSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "LockpickingSkillItem", "rsstats:skills/Lockpicking", "item.LockpickingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "DrivingSkillItem", "rsstats:skills/Driving", "item.DrivingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "FightingSkillItem", "rsstats:skills/Fighting", "item.FightingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "DisguiseSkillItem", "rsstats:skills/Disguise", "item.DisguiseSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "ThrowingSkillItem", "rsstats:skills/Throwing", "item.ThrowingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "PilotingSkillItem", "rsstats:skills/Piloting", "item.PilotingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "SwimmingSkillItem", "rsstats:skills/Swimming", "item.SwimmingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "ShootingSkillItem", "rsstats:skills/Shooting", "item.ShootingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "ShippingSkillItem", "rsstats:skills/Shipping", "item.ShippingSkillItem", (StatItem) statsInventory.getStackInSlot(1).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "GamblingSkillItem", "rsstats:skills/Gambling", "item.GamblingSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "PerceptionSkillItem", "rsstats:skills/Perception", "item.PerceptionSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "SurvivalSkillItem", "rsstats:skills/Survival", "item.SurvivalSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "TrackingSkillItem", "rsstats:skills/Tracking", "item.TrackingSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "MedicineSkillItem", "rsstats:skills/Medicine", "item.MedicineSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "ProvocationSkillItem", "rsstats:skills/Provocation", "item.ProvocationSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "InvestigationSkillItem", "rsstats:skills/Investigation", "item.InvestigationSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "RepearSkillItem", "rsstats:skills/Repear", "item.RepearSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "StreetFlairSkillItem", "rsstats:skills/StreetFlair", "item.StreetFlairSkillItem", (StatItem) statsInventory.getStackInSlot(2).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "IntimidationSkillItem", "rsstats:skills/Intimidation", "item.IntimidationSkillItem", (StatItem) statsInventory.getStackInSlot(4).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "DiplomacySkillItem", "rsstats:skills/Diplomacy", "item.DiplomacySkillItem", (StatItem) statsInventory.getStackInSlot(4).getItem())));
        skillsInventory.setInventorySlotContents(i++, new ItemStack(new SkillItem(skillBasicRolls, "ClimbingSkillItem", "rsstats:skills/Climbing", "item.ClimbingSkillItem", (StatItem) statsInventory.getStackInSlot(0).getItem())));
        skillsInventory.setCurrentTab("item.StrengthStatItem");
    }
}
