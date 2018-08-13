package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.StatCollector;
import rsstats.utils.DiceRoll;
import rsstats.utils.RollModifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static rsstats.common.RSStats.MODID;
import static rsstats.items.StatItems.*;

/**
 * Хранилище предметов-скиллов
 * @author RareScrap
 */
public class SkillItems {
    private static ArrayList<DiceRoll> skillDices;
    static {
        // TODO: Проверять на то, поставляется ли dices уже с модификаторами
        List<RollModifier> modificators = new ArrayList<RollModifier>();
        modificators.add(new RollModifier(-2, StatCollector.translateToLocal("modifiers.MissingSkill"))); // TODO: Разобраться почему это работает на сервере
        skillDices = new ArrayList<DiceRoll>();
        skillDices.add(new DiceRoll(null, null, 4, modificators)); // Создание дополнительного броска для нулевого уровня скиллов
        skillDices.add(new DiceRoll(null, null, 4));
        skillDices.add(new DiceRoll(null, null, 6));
        skillDices.add(new DiceRoll(null, null, 8));
        skillDices.add(new DiceRoll(null, null, 10));
        skillDices.add(new DiceRoll(null, null, 12));
    }

    // TODO: Слишком много литералов. Легко сделать ошибку, которую трудно отследить
    public static final SkillItem equitationSkillItem = new SkillItem(skillDices, "EquitationSkillItem", MODID + ":skills/Equitation", "item.EquitationSkillItem", agilityStatItem);
    public static final SkillItem lockpickingSkillItem = new SkillItem(skillDices, "LockpickingSkillItem", MODID + ":skills/Lockpicking", "item.LockpickingSkillItem", agilityStatItem);
    public static final SkillItem drivingSkillItem = new SkillItem(skillDices, "DrivingSkillItem", MODID + ":skills/Driving", "item.DrivingSkillItem", agilityStatItem);
    public static final SkillItem fightingSkillItem = new SkillItem(skillDices, "FightingSkillItem", MODID + ":skills/Fighting", "item.FightingSkillItem", agilityStatItem);
    public static final SkillItem disguiseSkillItem = new SkillItem(skillDices, "DisguiseSkillItem", MODID + ":skills/Disguise", "item.DisguiseSkillItem", agilityStatItem);
    public static final SkillItem throwingSkillItem = new SkillItem(skillDices, "ThrowingSkillItem", MODID + ":skills/Throwing", "item.ThrowingSkillItem", agilityStatItem);
    public static final SkillItem pilotingSkillItem = new SkillItem(skillDices, "PilotingSkillItem", MODID + ":skills/Piloting", "item.PilotingSkillItem", agilityStatItem);
    public static final SkillItem swimmingSkillItem = new SkillItem(skillDices, "SwimmingSkillItem", MODID + ":skills/Swimming", "item.SwimmingSkillItem", agilityStatItem);
    public static final SkillItem shootingSkillItem = new SkillItem(skillDices, "ShootingSkillItem", MODID + ":skills/Shooting", "item.ShootingSkillItem", agilityStatItem);
    public static final SkillItem shippingSkillItem = new SkillItem(skillDices, "ShippingSkillItem", MODID + ":skills/Shipping", "item.ShippingSkillItem", agilityStatItem);
    public static final SkillItem gamblingSkillItem = new SkillItem(skillDices, "GamblingSkillItem", MODID + ":skills/Gambling", "item.GamblingSkillItem", intelligenceStatItem);
    public static final SkillItem perceptionSkillItem = new SkillItem(skillDices, "PerceptionSkillItem", MODID + ":skills/Perception", "item.PerceptionSkillItem", intelligenceStatItem);
    public static final SkillItem survivalSkillItem = new SkillItem(skillDices, "SurvivalSkillItem", MODID + ":skills/Survival", "item.SurvivalSkillItem", intelligenceStatItem);
    public static final SkillItem trackingSkillItem = new SkillItem(skillDices, "TrackingSkillItem", MODID + ":skills/Tracking", "item.TrackingSkillItem", intelligenceStatItem);
    public static final SkillItem medicineSkillItem = new SkillItem(skillDices, "MedicineSkillItem", MODID + ":skills/Medicine", "item.MedicineSkillItem", intelligenceStatItem);
    public static final SkillItem provocationSkillItem = new SkillItem(skillDices, "ProvocationSkillItem", MODID + ":skills/Provocation", "item.ProvocationSkillItem", intelligenceStatItem);
    public static final SkillItem investigationSkillItem = new SkillItem(skillDices, "InvestigationSkillItem", MODID + ":skills/Investigation", "item.InvestigationSkillItem", intelligenceStatItem);
    public static final SkillItem repearSkillItem = new SkillItem(skillDices, "RepearSkillItem", MODID + ":skills/Repear", "item.RepearSkillItem", intelligenceStatItem);
    public static final SkillItem streetFlairSkillItem = new SkillItem(skillDices, "StreetFlairSkillItem", MODID + ":skills/StreetFlair", "item.StreetFlairSkillItem", intelligenceStatItem);
    public static final SkillItem intimidationSkillItem = new SkillItem(skillDices, "IntimidationSkillItem", MODID + ":skills/Intimidation", "item.IntimidationSkillItem", characterStatItem);
    public static final SkillItem diplomacySkillItem = new SkillItem(skillDices, "DiplomacySkillItem", MODID + ":skills/Diplomacy", "item.DiplomacySkillItem", characterStatItem);
    public static final SkillItem climbingSkillItem = new SkillItem(skillDices, "ClimbingSkillItem", MODID + ":skills/Climbing", "item.ClimbingSkillItem", strenghtStatItem);


    public static void registerItems() {
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

    /**
     * Возвращает скиллы в хранилище формата ИМЯ->СКИЛЛ
     */
    public static LinkedHashMap<String, SkillItem> getAll() {
        LinkedHashMap<String, SkillItem> skills = new LinkedHashMap<String, SkillItem>();
        skills.put("EquitationSkillItem", equitationSkillItem);
        skills.put("LockpickingSkillItem", lockpickingSkillItem);
        skills.put("DrivingSkillItem", drivingSkillItem);
        skills.put("FightingSkillItem", fightingSkillItem);
        skills.put("DisguiseSkillItem", disguiseSkillItem);
        skills.put("ThrowingSkillItem", throwingSkillItem);
        skills.put("PilotingSkillItem", pilotingSkillItem);
        skills.put("SwimmingSkillItem", swimmingSkillItem);
        skills.put("ShootingSkillItem", shootingSkillItem);
        skills.put("ShippingSkillItem", shippingSkillItem);
        skills.put("GamblingSkillItem", gamblingSkillItem);
        skills.put("PerceptionSkillItem", perceptionSkillItem);
        skills.put("SurvivalSkillItem", survivalSkillItem);
        skills.put("TrackingSkillItem", trackingSkillItem);
        skills.put("MedicineSkillItem", medicineSkillItem);
        skills.put("ProvocationSkillItem", provocationSkillItem);
        skills.put("InvestigationSkillItem", investigationSkillItem);
        skills.put("RepearSkillItem", repearSkillItem);
        skills.put("StreetFlairSkillItem", streetFlairSkillItem);
        skills.put("IntimidationSkillItem", intimidationSkillItem);
        skills.put("DiplomacySkillItem", diplomacySkillItem);
        skills.put("ClimbingSkillItem", climbingSkillItem);
        return skills;
    }
}
