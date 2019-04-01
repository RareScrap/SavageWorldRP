package rsstats.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержит удобные методы для работы с локализаций
 * @author RareScrap
 */
public class LangUtils {

    /**
     * @return Слово "и" с учетом локали клиента
     */
    @SideOnly(Side.CLIENT)
    public static String and() {
        return StatCollector.translateToLocal("lang.and");
    }

    /**
     * @return Слово "или" с учетом локали клиента
     */
    @SideOnly(Side.CLIENT)
    public static String or() {
        return StatCollector.translateToLocal("lang.or");
    }

    /**
     * То же что ли {@link ItemStack#getDisplayName()}, но без необходимости иметь стак
     * @param item Предмет, для которого нужно получить локализированное имя
     * @return Локализированное имя предмета
     */
    @SideOnly(Side.CLIENT)
    public static String getLocalizedName(Item item) {
        // см. ItemStack#getItemStackDisplayName()
        return ("" + StatCollector.translateToLocal(item.getUnlocalizedName() + ".name")).trim();
    }

    /**
     * То же самое что ли {@link StatCollector#translateToLocal(String)}, но с поддержкой переноса строк
     * @param traslateKey Ключ локалзации
     * @return Строки, разделенным символами "\n" (не символом преноса!).
     *         Если раздилителей нет, возвращает всю строку.
     */
    @SideOnly(Side.CLIENT)
    public static List<String> translateToLocal(String traslateKey) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String s : StatCollector.translateToLocal(traslateKey).split("\\\\n")) {
            arrayList.add(s);
        }
        return arrayList;
    }
}
