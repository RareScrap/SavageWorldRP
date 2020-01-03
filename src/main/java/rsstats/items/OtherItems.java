package rsstats.items;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.utils.LangUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Хранилище для неигровых предметов
 * @author RareScrap
 */
public class OtherItems {
    public static final Item flawsTabItem = new OtherItem().setTextureName(RSStats.MODID + ":other_items/flawsTab").setUnlocalizedName("FlawsTabItem");
    public static final Item perksTabItem = new OtherItem().setTextureName(RSStats.MODID + ":other_items/perksTab").setUnlocalizedName("PerksTabItem");
    public static final Item positiveEffectsTabItem = new OtherItem().setTextureName(RSStats.MODID + ":other_items/positiveEffectsTab").setUnlocalizedName("PositiveEffectsTabItem");
    public static final Item negativeEffectsTabItem = new OtherItem().setTextureName(RSStats.MODID + ":other_items/negativeEffectsTab").setUnlocalizedName("NegativeEffectsTabItem");

    public static void registerItems() {
        GameRegistry.registerItem(perksTabItem, perksTabItem.getUnlocalizedName());
        GameRegistry.registerItem(flawsTabItem, flawsTabItem.getUnlocalizedName());
        GameRegistry.registerItem(positiveEffectsTabItem, positiveEffectsTabItem.getUnlocalizedName());
        GameRegistry.registerItem(negativeEffectsTabItem, negativeEffectsTabItem.getUnlocalizedName());
    }

    public static class OtherItem extends Item {
        // Получаем информацию предмета из файла локализации
        @Override
        public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List infoLines, boolean p_77624_4_) {
            infoLines.addAll(LangUtils.translateToLocal(getUnlocalizedName() + ".lore"));
        }
    }

    /**
     * Возвращает итемы в хранилище формата ИМЯ->ИТЕМ
     */
    public static LinkedHashMap<String, Item> getAll() {
        LinkedHashMap<String, Item> items = new LinkedHashMap<String, Item>();
        items.put(perksTabItem.getUnlocalizedName(), perksTabItem);
        items.put(flawsTabItem.getUnlocalizedName(), flawsTabItem);
        items.put(positiveEffectsTabItem.getUnlocalizedName(), positiveEffectsTabItem);
        items.put(negativeEffectsTabItem.getUnlocalizedName(), negativeEffectsTabItem);
        return items;
    }
}
