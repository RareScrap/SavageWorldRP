package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItems;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.roll.RollModifier;

import java.util.List;
import java.util.Map;

import static rsstats.data.ExtendedPlayer.Rank;
import static rsstats.utils.LangUtils.and;
import static rsstats.utils.LangUtils.getLocalizedName;

public class BigBoy extends PerkItem {
    public BigBoy() {
        setUnlocalizedName("BigBoyPerkItem");
        setTextureName(RSStats.MODID + ":perks/bigboy");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(getLocalizedName(StatItems.strenghtStatItem) + " d6+ " + and()
                + " " + getLocalizedName(StatItems.enduranceStatItem) + " d6+");
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return player.rank.moreOrEqual(Rank.NOVICE)
                && player.getStatLvl(StatItems.strenghtStatItem) >= 2
                && player.getStatLvl(StatItems.enduranceStatItem) >= 2;

    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        Map<IModifierDependent, RollModifier> modifiers = super.getModifiers();
        modifiers.put(ExtendedPlayer.ParamKeys.PERSISTENCE, new RollModifier(+1, getUnlocalizedName()+".name"));
        // TODO: carry weight modifier
        return modifiers;
    }
}
