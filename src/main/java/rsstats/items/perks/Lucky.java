package rsstats.items.perks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.events.NewSessionEvent;
import rsstats.items.MiscItems;
import rsstats.items.PerkItems;
import rsstats.items.perk.PerkItem;

import java.util.List;

public class Lucky extends PerkItem {

    public Lucky() {
        setUnlocalizedName("LuckyPerkItem");
        setTextureName(RSStats.MODID + ":perks/lucky");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return player.rank.moreOrEqual(ExtendedPlayer.Rank.NOVICE);
    }

    @SubscribeEvent
    public void onNewSession(NewSessionEvent event) { // TODO: НЕ ТЕСТИРОВАЛОСЬ!
        if (event.extendedPlayer.hasPerk(PerkItems.lucky))
            event.entityPlayer.inventory.addItemStackToInventory(new ItemStack(MiscItems.rerollCoinItem));
    }
}
