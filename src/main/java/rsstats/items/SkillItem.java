package rsstats.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import rsstats.api.roll.Roll;

import java.util.ArrayList;
import java.util.List;

/**
 * Предмет
 * @author rares
 */
public class SkillItem extends StatItem {
    /** Общее количество уровней навыка */
    private static int NUMBER_OF_LEVELS = 6;
    
    /** Хранилище иконов для каждого уровня скилла */
    private IIcon[] icons = new IIcon[NUMBER_OF_LEVELS]; // хранилище иконок для всего семейства предмета
    
    
    /** Стата, к которой принадлежит навык */
    public StatItem parentStat;

    
    public SkillItem(ArrayList<Roll> basicRolls, String unlocalizedName, String registerIconPrefix, String localePrefix, StatItem parentStat) {
        super(basicRolls, unlocalizedName, registerIconPrefix, localePrefix);
        this.parentStat = parentStat;
        this.damageMinLimit = 0;
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
        super.addInformation(itemstack, player, list, par4);
    }
    
    /**
     * Регистрирует иконку для каждого подтипа статы
     * @param reg TODO: Добавить Javadoc
     */
    @Override
    public void registerIcons(IIconRegister reg) {
        for (int i = 0; i < this.icons.length; ++i) {
            this.icons[i] = reg.registerIcon(registerIconPrefix + i);
        }
    }
    
    /**
     * Создание субтипов (уровней) статы
     * @param item TODO: Добавить Javadoc
     * @param tab TODO: Добавить Javadoc
     * @param list TODO: Добавить Javadoc
     */
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < this.icons.length; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }
    
    /**
     * Возвращает иконку, соответствующую субтипу.
     * @param meta Порядковый номер субтипа
     * @return Икнока, соответвующая субтипу
     */
    @Override
    public IIcon getIconFromDamage(int meta) {
        if (meta > this.icons.length) {
            meta = 0;
        }
        return this.icons[meta];
    }
    
    /**
     * Создает анимацию зачарования к последнему субтипу.
     * Нужно для того, чтобы красиво выделить максимальный уровень статы.
     * @param par1ItemStack TODO: Добавить Javadoc
     * @return True, если предмету нужно включить анимацию зачарования. Иначе - false.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack par1ItemStack) {
         //par1ItemStack.setTagInfo("ench", new NBTTagList());
         if (par1ItemStack.getItemDamage() == NUMBER_OF_LEVELS-1)
            return true;
         else
             return false;
    }
    
    
    
}
