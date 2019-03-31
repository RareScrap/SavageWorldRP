package rsstats.items.perks;

import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.SkillItems;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.items.perk.Requirement;
import rsstats.roll.RollModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vigilance extends PerkItem {
    public Vigilance() {
        setUnlocalizedName("VigilancePerkItem");
        setTextureName(RSStats.MODID + ":perks/vigilance");
    }

    @Override
    public List<Requirement> getRequirements() {
        List<Requirement> requirements = new ArrayList<Requirement>();
        requirements.add(new Requirement.Rank(ExtendedPlayer.Rank.NOVICE));
        return requirements;
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        HashMap<IModifierDependent, RollModifier> modifiers = new HashMap<IModifierDependent, RollModifier>();
        modifiers.put(SkillItems.perceptionSkillItem, new RollModifier(+2, "item.VigilancePerkItem.name"));
        return modifiers;
    }
}
