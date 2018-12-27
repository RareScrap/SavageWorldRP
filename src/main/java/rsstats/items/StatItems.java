package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import rsstats.roll.Roll;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static rsstats.common.RSStats.MODID;

/**
 * Хранилище предметов-статов
 * @author RareScrap
 */
public class StatItems {
    public static ArrayList<Roll> basicRolls;
    static { // TODO: Они же должны быть только на сервере
        basicRolls = new ArrayList<Roll>();
        basicRolls.add(new Roll(4));
        basicRolls.add(new Roll(6));
        basicRolls.add(new Roll(8));
        basicRolls.add(new Roll(10));
        basicRolls.add(new Roll(12));
    }

    public static final StatItem strenghtStatItem = new StatItem(basicRolls, "StrengthStatItem", MODID + ":strenght", "item.StrengthStatItem"); // 3 - rarescrap:StrenghtIcon_
    public static final StatItem agilityStatItem = new StatItem(basicRolls, "AgilityStatItem", MODID + ":agility", "item.AgilityStatItem");
    public static final StatItem intelligenceStatItem = new StatItem(basicRolls, "IntelligenceStatItem", MODID + ":intelligence", "item.IntelligenceStatItem");
    public static final StatItem enduranceStatItem = new StatItem(basicRolls, "EnduranceStatItem", MODID + ":endurance", "item.EnduranceStatItem");
    public static final StatItem characterStatItem = new StatItem(basicRolls, "CharacterStatItem", MODID + ":character", "item.CharacterStatItem");

    public static void registerItems() {
        GameRegistry.registerItem(strenghtStatItem, "StrengthStatItem");
        GameRegistry.registerItem(agilityStatItem, "AgilityStatItem");
        GameRegistry.registerItem(intelligenceStatItem, "IntelligenceStatItem");
        GameRegistry.registerItem(enduranceStatItem, "EnduranceStatItem");
        GameRegistry.registerItem(characterStatItem, "CharacterStatItem");
    }

    // TODO: юзать Map или Enum?
    /**
     * Возвращает статы в хранилище формата ИМЯ->СТАТА
     */
    public static LinkedHashMap<String, StatItem> getAll() {
        LinkedHashMap<String, StatItem> stats = new LinkedHashMap<String, StatItem>();
        stats.put("StrengthStatItem", strenghtStatItem);
        stats.put("AgilityStatItem", agilityStatItem);
        stats.put("IntelligenceStatItem", intelligenceStatItem);
        stats.put("EnduranceStatItem", enduranceStatItem);
        stats.put("CharacterStatItem", characterStatItem);
        return stats;
    }
}
