package rsstats.items.perks;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.PerkItems;
import rsstats.api.items.perk.PerkItem;

import java.util.List;

import static rsstats.utils.LangUtils.getLocalizedName;

public class Thrifty extends PerkItem {

    public Thrifty() {
        setUnlocalizedName("ThriftyPerkItem");
        setTextureName(RSStats.MODID + ":perks/thrifty");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(getLocalizedName(PerkItems.lucky));
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return super.isSuitableFor(player) && player.hasPerk(PerkItems.lucky);
    }

    @Override
    public boolean canActivate() {
        return true;
    }

    @Override
    public void activate(ExtendedPlayer player) {
        if (player.getEntityPlayer().worldObj.isRemote) return;

        ChatStyle style = new ChatStyle().setColor(EnumChatFormatting.GOLD);
        ChatComponentTranslation msg = new ChatComponentTranslation(
                getUnlocalizedName()+".activate_msg", player.getEntityPlayer().getDisplayName());
        msg.setChatStyle(style);
        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(msg);
        super.activate(player);
    }
}
