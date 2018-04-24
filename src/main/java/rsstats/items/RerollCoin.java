package rsstats.items;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import rsstats.common.RSStats;
import rsstats.utils.DescriptionCutter;

import java.util.Arrays;
import java.util.List;

public class RerollCoin extends Item{
    private static final String MESSAGE_LOCALE_KEY = "item.RerollCoinItem";
    private IIcon icon;

    public RerollCoin(String unlocalizedName) {
        this.setMaxStackSize(64);
        this.setCreativeTab(RSStats.CREATIVE_TAB);
        this.setHasSubtypes(false);
        this.setUnlocalizedName(unlocalizedName);
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = reg.registerIcon("rsstats:reroll_coin");
    }

    // TODO: Игнорирует meta
    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
        return true;
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Выводит в серверный чат сообщение о
     * намерение игрока перебросить бросок.
     * @param itemStack
     * @param word
     * @param entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World word, EntityPlayer entityPlayer) {
        if (!word.isRemote) {
            String msg = StatCollector.translateToLocalFormatted(MESSAGE_LOCALE_KEY + ".msg", entityPlayer.getDisplayName());
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsg(
                    new ChatComponentText(msg)
            );
        }
        // TODO: Не работает в креативе (скорее всего из-за InvTweak)
        itemStack.stackSize--;
        return super.onItemRightClick(itemStack, word, entityPlayer);
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     *
     * @param p_77624_1_
     * @param p_77624_2_
     * @param list
     * @param p_77624_4_
     */
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List list, boolean p_77624_4_) {
        String[] strs = DescriptionCutter.cut(4, StatCollector.translateToLocal(MESSAGE_LOCALE_KEY + ".lore"));
        list.addAll(Arrays.asList(strs));
        super.addInformation(p_77624_1_, p_77624_2_, list, p_77624_4_);
    }

}
