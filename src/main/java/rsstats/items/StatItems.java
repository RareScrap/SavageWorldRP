package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import rsstats.utils.DiceRoll;

import java.util.ArrayList;

import static rsstats.common.RSStats.MODID;

/**
 * Хранилище предметов-статов
 * @author RareScrap
 */
public class StatItems {
    private static ArrayList<DiceRoll> statDices;
    static {
        statDices = new ArrayList<DiceRoll>();
        statDices.add(new DiceRoll(null, null, 4));
        statDices.add(new DiceRoll(null, null, 6));
        statDices.add(new DiceRoll(null, null, 8));
        statDices.add(new DiceRoll(null, null, 10));
        statDices.add(new DiceRoll(null, null, 12));
    }

    public static final StatItem strenghtStatItem = new StatItem(statDices, "StrengthStatItem", MODID + ":strenght", "item.StrengthStatItem"); // 3 - rarescrap:StrenghtIcon_
    public static final StatItem agilityStatItem = new StatItem(statDices, "AgilityStatItem", MODID + ":agility", "item.AgilityStatItem");
    public static final StatItem intelligenceStatItem = new StatItem(statDices, "IntelligenceStatItem", MODID + ":intelligence", "item.IntelligenceStatItem");
    public static final StatItem enduranceStatItem = new StatItem(statDices, "EnduranceStatItem", MODID + ":endurance", "item.EnduranceStatItem");
    public static final StatItem characterStatItem = new StatItem(statDices, "CharacterStatItem", MODID + ":character", "item.CharacterStatItem");

    public static void registerItems() {
        GameRegistry.registerItem(strenghtStatItem, "StrengthStatItem");
        GameRegistry.registerItem(agilityStatItem, "AgilityStatItem");
        GameRegistry.registerItem(intelligenceStatItem, "IntelligenceStatItem");
        GameRegistry.registerItem(enduranceStatItem, "EnduranceStatItem");
        GameRegistry.registerItem(characterStatItem, "CharacterStatItem");
    }
}
