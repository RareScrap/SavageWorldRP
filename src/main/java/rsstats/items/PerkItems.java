package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import rsstats.api.items.perk.PerkItem;
import rsstats.items.perks.*;
import rsstats.utils.LangUtils;

import java.util.LinkedHashMap;

/**
 * Хранилище для предметов перков
 */
public class PerkItems {
    public static final PerkItem aristocrat = new AristocratPerk();
    public static final PerkItem vigilance = new Vigilance();
    public static final PerkItem riches = new Riches();
    public static final PerkItem riches2 = new Riches2();
    public static final PerkItem bigboy = new BigBoy();
    public static final PerkItem swiftness = new Swiftness();
    public static final PerkItem lucky = new Lucky();
    public static final PerkItem lucky2 = new Lucky2();
    public static final PerkItem faithfulBeast = new FaithfulBeast();
    public static final PerkItem breathOfCourage = new BreathOfCourage();
    public static final PerkItem thrifty = new Thrifty();

    public static void registerItems() {
        GameRegistry.registerItem(aristocrat, "AristocratPerkItem");
        GameRegistry.registerItem(vigilance, "VigilancePerkItem");
        GameRegistry.registerItem(riches, "RichesPerkItem");
        GameRegistry.registerItem(riches2, "Riches2PerkItem");
        GameRegistry.registerItem(bigboy, "BigboyPerkItem");
        GameRegistry.registerItem(swiftness, "SwiftnessPerkItem");
        GameRegistry.registerItem(lucky, "LuckyPerkItem");
        GameRegistry.registerItem(lucky2, "Lucky2PerkItem");
        GameRegistry.registerItem(faithfulBeast, "FaithfulBeastPerkItem");
        GameRegistry.registerItem(breathOfCourage, "BreathOfCouragePerkItem");
        GameRegistry.registerItem(thrifty, "ThriftyPerkItem");
    }

    /**
     * Возвращает скиллы в хранилище формата ИМЯ->СКИЛЛ
     */
    public static LinkedHashMap<String, PerkItem> getAll() {
        LinkedHashMap<String, PerkItem> skills = new LinkedHashMap<String, PerkItem>();
        skills.put(aristocrat.getUnlocalizedName(), aristocrat);
        skills.put(vigilance.getUnlocalizedName(), vigilance);
        skills.put(riches.getUnlocalizedName(), riches);
        skills.put(riches2.getUnlocalizedName(), riches2);
        skills.put(bigboy.getUnlocalizedName(), bigboy);
        skills.put(swiftness.getUnlocalizedName(), swiftness);
        skills.put(lucky.getUnlocalizedName(), lucky);
        skills.put(lucky2.getUnlocalizedName(), lucky2);
        skills.put(faithfulBeast.getUnlocalizedName(), faithfulBeast);
        skills.put(breathOfCourage.getUnlocalizedName(), breathOfCourage);
        skills.put(thrifty.getUnlocalizedName(), thrifty);
        return skills;
    }

    @SideOnly(Side.CLIENT)
    public static String or(PerkItem... perkItems) { // TODO: Только перкитемы?
        StringBuffer buffer = new StringBuffer();
        String or = LangUtils.or();
        for (int i = 0; i < perkItems.length; i++) {
            PerkItem perkItem = perkItems[i];
            buffer.append(LangUtils.getLocalizedName(perkItem));
            if (i < perkItems.length - 1) {
                buffer.append(" ")
                        .append(or)
                        .append(" ");
            }
        }
        return buffer.toString();
    }
}
