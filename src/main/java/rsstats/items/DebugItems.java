package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.StatCollector;
import rsstats.roll.BasicRoll;
import rsstats.roll.RollModifier;

import java.util.ArrayList;

import static rsstats.items.StatItems.*;

/**
 * Хранилище предметов-скиллов, предназначенных для дебага
 * @author RareScrap
 */
public class DebugItems {
    public static final SkillItem[] skillItems_Strength = new SkillItem[15];
    public static final SkillItem[] skillItems_Agility = new SkillItem[15];
    public static final SkillItem[] skillItems_Intelligence = new SkillItem[15];
    public static final SkillItem[] skillItems_Endurance = new SkillItem[15];
    public static final SkillItem[] skillItems_Character = new SkillItem[15];
    public static final SkillItem[][] ii = new SkillItem[][] {skillItems_Strength, skillItems_Agility, skillItems_Intelligence, skillItems_Endurance, skillItems_Character};

    static {
        RollModifier level_zero = new RollModifier(-2, StatCollector.translateToLocal("modifiers.MissingSkill")); // TODO: Починить локализацию с учетом клиент-серверной архитектурой

        ArrayList<BasicRoll> skillDices = new ArrayList<BasicRoll>();
        skillDices.add(new BasicRoll(4, level_zero)); // Создание дополнительного броска для нулевого уровня скиллов
        skillDices.add(new BasicRoll(4));
        skillDices.add(new BasicRoll(6));
        skillDices.add(new BasicRoll(8));
        skillDices.add(new BasicRoll(10));
        skillDices.add(new BasicRoll(12));

        StatItem[] parents = new StatItem[] {strenghtStatItem, agilityStatItem, intelligenceStatItem, enduranceStatItem, characterStatItem};
        for (int i1 = 0; i1 < ii.length; i1++) {
            SkillItem[] t = ii[i1];
            StatItem parent = parents[i1];
            for (int i = 0; i < t.length; i++) {
                SkillItem skillItem = new SkillItem(skillDices, "DebugSkillItem_" + parent.getUnlocalizedName() + i, "rsstats:skills/Debugging", "item.DebugSkillItem", parent);
                t[i] = skillItem;
            }
        }
    }

    public static void registerDebugItems() {
        for (SkillItem[] skillsFamily : ii) {
            for (SkillItem skillItem : skillsFamily) {
                GameRegistry.registerItem(skillItem, skillItem.getUnlocalizedName());
            }
        }
    }
}
