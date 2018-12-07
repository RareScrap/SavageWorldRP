package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import rsstats.items.perk.PerkItem;
import rsstats.items.perks.AristocratPerk;
import rsstats.items.perks.Vigilance;

import java.util.LinkedHashMap;

/**
 * Хранилище для предметов перков
 */
public class PerkItems {
    public static final PerkItem aristocrat = new AristocratPerk();
    public static final PerkItem vigilance = new Vigilance();

    public static void registerItems() {
        GameRegistry.registerItem(aristocrat, "AristocratPerkItem");
        GameRegistry.registerItem(vigilance, "VigilancePerkItem");
    }

    /**
     * Возвращает скиллы в хранилище формата ИМЯ->СКИЛЛ
     */
    public static LinkedHashMap<String, PerkItem> getAll() {
        LinkedHashMap<String, PerkItem> skills = new LinkedHashMap<String, PerkItem>();
        skills.put(aristocrat.getUnlocalizedName(), aristocrat);
        return skills;
    }
}
