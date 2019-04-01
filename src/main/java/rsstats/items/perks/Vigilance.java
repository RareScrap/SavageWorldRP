package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.SkillItems;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.roll.RollModifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vigilance extends PerkItem {
    public Vigilance() {
        setUnlocalizedName("VigilancePerkItem");
        setTextureName(RSStats.MODID + ":perks/vigilance");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.get(player).rank.getTranslatedString()); // TODO: Тут будет ошибка, если игрок будет просматривать перки друого игрока. Пока оставляем так, т.к. на данный момент нельзя смотреть перки другого игрока.
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return player.rank.moreOrEqual(ExtendedPlayer.Rank.NOVICE);
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        HashMap<IModifierDependent, RollModifier> modifiers = new HashMap<IModifierDependent, RollModifier>();
        modifiers.put(SkillItems.perceptionSkillItem, new RollModifier(+2, "item.VigilancePerkItem.name"));
        return modifiers;
    }
}
