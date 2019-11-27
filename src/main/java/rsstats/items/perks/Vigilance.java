package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.items.SkillItems;
import rsstats.api.items.perk.IModifierDependent;
import rsstats.api.items.perk.PerkItem;
import rsstats.api.roll.RollModifier;

import java.util.List;
import java.util.Map;

import static rsstats.data.ExtendedPlayer.Rank;

public class Vigilance extends PerkItem {
    public Vigilance() {
        setUnlocalizedName("VigilancePerkItem");
        setTextureName(RSStats.MODID + ":perks/vigilance");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(Rank.NOVICE.getTranslatedString());
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        Map<IModifierDependent, RollModifier> modifiers = super.getModifiers();
        modifiers.put(SkillItems.perceptionSkillItem, new RollModifier(+2, getUnlocalizedName()+".name"));
        return modifiers;
    }
}
