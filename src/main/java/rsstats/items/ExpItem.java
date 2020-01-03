package rsstats.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.common.RSStats;
import rsstats.utils.LangUtils;

import java.util.List;

public class ExpItem extends Item {
    private IIcon icon;
    protected final String generalPrefix = "item.ExpItem";

    public ExpItem(String unlocalizedName) {
        this.setMaxStackSize(64);
        this.setCreativeTab(RSStats.CREATIVE_TAB);
        this.setHasSubtypes(false);
        this.setUnlocalizedName(unlocalizedName);
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = reg.registerIcon("rsstats:exp");
    }

    // TODO: Игнорирует meta
    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @Override
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }

    /**
     * Добавляет к предмету пояснение.
     * @param itemstack TODO: Добавить Javadoc
     * @param player TODO: Добавить Javadoc
     * @param list TODO: Добавить Javadoc
     * @param par4 TODO: Добавить Javadoc
     */
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
        // Пустая строка-разделитель
        //list.add("");
        list.addAll(LangUtils.translateToLocal(generalPrefix + ".lore"));
    }
}
