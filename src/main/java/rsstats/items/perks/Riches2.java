package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.PerkItems;
import rsstats.api.items.perk.PerkItem;

import java.util.List;

import static rsstats.items.PerkItems.aristocrat;
import static rsstats.items.PerkItems.riches;

public class Riches2 extends PerkItem {

    public Riches2() {
        setUnlocalizedName("Riches2PerkItem");
        setTextureName(RSStats.MODID + ":perks/riches+");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(PerkItems.or(riches, aristocrat));
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return super.isSuitableFor(player) && (player.hasPerk(riches) || player.hasPerk(aristocrat));
    }
}
