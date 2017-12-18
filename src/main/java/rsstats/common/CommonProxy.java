/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsstats.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import rsstats.common.event.KeyHandler;
import rsstats.common.network.PacketOpenRSStatsInventory;
import rsstats.common.network.PacketOpenSSPPage;
import rsstats.common.network.PacketShowSkillsByStat;
import rsstats.common.network.RollPacketToServer;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import rsstats.inventory.container.StatsContainer;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.utils.DiceRoll;
import rsstats.utils.RollModifier;

import java.util.ArrayList;
import java.util.List;

import static rsstats.common.RSStats.instance;
import static rsstats.common.RSStats.proxy;

/**
 * Проки, содержащий код как для клиента, так и сервера
 * @author RareScrap
 */
public class CommonProxy implements IGuiHandler {
    /** Обработчик нажатия кнопок, используемых для вызова GUI */
    protected KeyHandler keyHandler;
    /** Обертка для работы с сетью */
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(RSStats.MODID.toLowerCase());

    public void preInit(FMLPreInitializationEvent event, ArrayList<DiceRoll> dices) {
        // Когда сообщений станет много, их можно вынести в отдельный класс в метод init()
        INSTANCE.registerMessage(RollPacketToServer.MessageHandler.class, RollPacketToServer.class, 0, Side.SERVER); // Регистрация сообщения о пробросе статы
        INSTANCE.registerMessage(PacketOpenRSStatsInventory.MessageHandler.class, PacketOpenRSStatsInventory.class, 1, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenSSPPage.MessageHandler.class, PacketOpenSSPPage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(PacketShowSkillsByStat.MessageHandler.class, PacketShowSkillsByStat.class, 3, Side.SERVER);

        // Инициализация предметов статов
        StatItem strenghtStatItem = new StatItem(dices, "StrengthStatItem", "rsstats:strenght", "item.StrengthStatItem"); // 3 - rarescrap:StrenghtIcon_
        StatItem agilityStatItem = new StatItem(dices, "AgilityStatItem", "rsstats:agility", "item.AgilityStatItem");
        StatItem intelligenceStatItem = new StatItem(dices, "IntelligenceStatItem", "rsstats:intelligence", "item.IntelligenceStatItem");
        StatItem enduranceStatItem = new StatItem(dices, "EnduranceStatItem", "rsstats:endurance", "item.EnduranceStatItem");
        StatItem characterStatItem = new StatItem(dices, "CharacterStatItem", "rsstats:character", "item.CharacterStatItem");
        // Регистрация предметов статов
        GameRegistry.registerItem(strenghtStatItem, "StrenghtStatItem");
        GameRegistry.registerItem(agilityStatItem, "AgilityStatItem");
        GameRegistry.registerItem(intelligenceStatItem, "IntelligenceStatItem");
        GameRegistry.registerItem(enduranceStatItem, "EnduranceStatItem");
        GameRegistry.registerItem(characterStatItem, "CharacterStatItem");

        // Создание дополнительно броска для нулевого уровня скиллов
        // TODO: Проверять на то, поставляется ли dices уже с модификаторами
        List<RollModifier> modificators = new ArrayList<RollModifier>();
        // TODO: Локализировать эту строку
        modificators.add(new RollModifier(-2, "Отсуствующий навык"));
        dices.add(0, new DiceRoll(dices.get(0), dices.get(0).getPlayerName(), dices.get(0).getRollName(), modificators));

        // Инициализация предметов склиллов
        SkillItem equitationSkillItem = new SkillItem(dices, "EquitationSkillItem", "rsstats:skills/Equitation", "item.EquitationSkillItem", agilityStatItem);
        SkillItem lockpickingSkillItem = new SkillItem(dices, "LockpickingSkillItem", "rsstats:skills/Lockpicking", "item.LockpickingSkillItem", agilityStatItem);
        SkillItem drivingSkillItem = new SkillItem(dices, "DrivingSkillItem", "rsstats:skills/Driving", "item.DrivingSkillItem", agilityStatItem);
        SkillItem fightingSkillItem = new SkillItem(dices, "FightingSkillItem", "rsstats:skills/Fighting", "item.FightingSkillItem", agilityStatItem);
        SkillItem disguiseSkillItem = new SkillItem(dices, "DisguiseSkillItem", "rsstats:skills/Disguise", "item.DisguiseSkillItem", agilityStatItem);
        SkillItem throwingSkillItem = new SkillItem(dices, "ThrowingSkillItem", "rsstats:skills/Throwing", "item.ThrowingSkillItem", agilityStatItem);
        SkillItem pilotingSkillItem = new SkillItem(dices, "PilotingSkillItem", "rsstats:skills/Piloting", "item.PilotingSkillItem", agilityStatItem);
        SkillItem swimmingSkillItem = new SkillItem(dices, "SwimmingSkillItem", "rsstats:skills/Swimming", "item.SwimmingSkillItem", agilityStatItem);
        SkillItem shootingSkillItem = new SkillItem(dices, "ShootingSkillItem", "rsstats:skills/Shooting", "item.ShootingSkillItem", agilityStatItem);
        SkillItem shippingSkillItem = new SkillItem(dices, "ShippingSkillItem", "rsstats:skills/Shipping", "item.ShippingSkillItem", agilityStatItem);
        SkillItem gamblingSkillItem = new SkillItem(dices, "GamblingSkillItem", "rsstats:skills/Gambling", "item.GamblingSkillItem", intelligenceStatItem);
        SkillItem perceptionSkillItem = new SkillItem(dices, "PerceptionSkillItem", "rsstats:skills/Perception", "item.PerceptionSkillItem", intelligenceStatItem);
        SkillItem survivalSkillItem = new SkillItem(dices, "SurvivalSkillItem", "rsstats:skills/Survival", "item.SurvivalSkillItem", intelligenceStatItem);
        SkillItem trackingSkillItem = new SkillItem(dices, "TrackingSkillItem", "rsstats:skills/Tracking", "item.TrackingSkillItem", intelligenceStatItem);
        SkillItem medicineSkillItem = new SkillItem(dices, "MedicineSkillItem", "rsstats:skills/Medicine", "item.MedicineSkillItem", intelligenceStatItem);
        SkillItem provocationSkillItem = new SkillItem(dices, "ProvocationSkillItem", "rsstats:skills/Provocation", "item.ProvocationSkillItem", intelligenceStatItem);
        SkillItem investigationSkillItem = new SkillItem(dices, "InvestigationSkillItem", "rsstats:skills/Investigation", "item.InvestigationSkillItem", intelligenceStatItem);
        SkillItem repearSkillItem = new SkillItem(dices, "RepearSkillItem", "rsstats:skills/Repear", "item.RepearSkillItem", intelligenceStatItem);
        SkillItem streetFlairSkillItem = new SkillItem(dices, "StreetFlairSkillItem", "rsstats:skills/StreetFlair", "item.StreetFlairSkillItem", intelligenceStatItem);
        SkillItem intimidationSkillItem = new SkillItem(dices, "IntimidationSkillItem", "rsstats:skills/Intimidation", "item.IntimidationSkillItem", characterStatItem);
        SkillItem diplomacySkillItem = new SkillItem(dices, "DiplomacySkillItem", "rsstats:skills/Diplomacy", "item.DiplomacySkillItem", characterStatItem);
        SkillItem climbingSkillItem = new SkillItem(dices, "ClimbingSkillItem", "rsstats:skills/Climbing", "item.ClimbingSkillItem", strenghtStatItem);

        // Регистрация предметов скиллов
        GameRegistry.registerItem(equitationSkillItem, "EquitationSkillItem");
        GameRegistry.registerItem(lockpickingSkillItem, "LockpickingSkillItem");
        GameRegistry.registerItem(drivingSkillItem, "DrivingSkillItem");
        GameRegistry.registerItem(fightingSkillItem, "FightingSkillItem");
        GameRegistry.registerItem(disguiseSkillItem, "DisguiseSkillItem");
        GameRegistry.registerItem(throwingSkillItem, "ThrowingSkillItem");
        GameRegistry.registerItem(pilotingSkillItem, "PilotingSkillItem");
        GameRegistry.registerItem(swimmingSkillItem, "SwimmingSkillItem");
        GameRegistry.registerItem(shootingSkillItem, "ShootingSkillItem");
        GameRegistry.registerItem(shippingSkillItem, "ShippingSkillItem");
        GameRegistry.registerItem(gamblingSkillItem, "GamblingSkillItem");
        GameRegistry.registerItem(perceptionSkillItem, "PerceptionSkillItem");
        GameRegistry.registerItem(survivalSkillItem, "SurvivalSkillItem");
        GameRegistry.registerItem(trackingSkillItem, "TrackingSkillItem");
        GameRegistry.registerItem(medicineSkillItem, "MedicineSkillItem");
        GameRegistry.registerItem(provocationSkillItem, "ProvocationSkillItem");
        GameRegistry.registerItem(investigationSkillItem, "InvestigationSkillItem");
        GameRegistry.registerItem(repearSkillItem, "RepearSkillItem");
        GameRegistry.registerItem(streetFlairSkillItem, "StreetFlairSkillItem");
        GameRegistry.registerItem(intimidationSkillItem, "IntimidationSkillItem");
        GameRegistry.registerItem(diplomacySkillItem, "DiplomacySkillItem");
        GameRegistry.registerItem(climbingSkillItem, "ClimbingSkillItem");
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        registerKeyBindings();
    }

    public void postInit(FMLPostInitializationEvent event) {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case RSStats.GUI:
                return new MainContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory, ExtendedPlayer.get(player).skillsInventory);
            case RSStats.SSP_UI_CODE:
                return new StatsContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory);

        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null; // Переопределяется в ClientProxy
    }
    
    // Переопределяется в ClientProxy
    public void registerKeyBindings() {}

    
    
}
