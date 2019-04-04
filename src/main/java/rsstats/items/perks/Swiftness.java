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

import static rsstats.utils.LangUtils.getLocalizedName;

public class Swiftness extends PerkItem {

    public Swiftness() {
        setUnlocalizedName("SwiftnessPerkItem");
        setTextureName(RSStats.MODID + ":perks/swiftness");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(getLocalizedName(StatItems.agilityStatItem) + " d6+");
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return super.isSuitableFor(player) && player.getStatLvl(StatItems.agilityStatItem) >= 2;
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        Map<IModifierDependent, RollModifier> modifiers = super.getModifiers();
        modifiers.put(ExtendedPlayer.ParamKeys.STEP, new RollModifier(+2, getUnlocalizedName()+".name"));
        // TODO: Бег повысить на 2 ступени (обычно с d6 до d10)
        return modifiers;
    }
}
