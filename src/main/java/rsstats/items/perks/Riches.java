package rsstats.items.perks;

import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.items.perk.Requirement;
import rsstats.roll.RollModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Riches extends PerkItem {

    public Riches() {
        setUnlocalizedName("RichesPerkItem");
        setTextureName(RSStats.MODID + ":perks/riches");
    }

    @Override
    public List<Requirement> getRequirements() {
        ArrayList<Requirement> requirements = new ArrayList<Requirement>();
        requirements.add(new Requirement.Rank(ExtendedPlayer.Rank.NOVICE));
        return requirements;
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        return new HashMap<IModifierDependent, RollModifier>(); // null?
    }
}
