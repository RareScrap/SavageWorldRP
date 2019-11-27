package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.api.items.perk.PerkItem;

import java.util.List;

import static rsstats.data.ExtendedPlayer.Rank;

public class Riches extends PerkItem { // TODO: Уровни перков делать ли через метадату итема?

    public Riches() {
        setUnlocalizedName("RichesPerkItem");
        setTextureName(RSStats.MODID + ":perks/riches");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(Rank.NOVICE.getTranslatedString());
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }
}
