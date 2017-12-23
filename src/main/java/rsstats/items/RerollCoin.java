package rsstats.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.common.RSStats;

public class RerollCoin extends Item{
    private IIcon icon;

    public RerollCoin() {
        this.setMaxStackSize(1);
        this.setCreativeTab(RSStats.CREATIVE_TAB);
        this.setHasSubtypes(false);
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
}
