package rsstats.items.perks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.perk.PerkItem;

import java.util.List;

public class FaithfulBeast extends PerkItem {

    public FaithfulBeast() {
        setUnlocalizedName("FaithfulBeastPerkItem");
        setTextureName(RSStats.MODID + ":perks/faithful_beast");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    // TODO: Я хз как сделать функционал этому перку в данной стадии готовности мода
}
