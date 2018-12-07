package rsstats.data;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import rsstats.common.RSStats;
import rsstats.items.StatItem;
import rsstats.items.perk.IModifierDependent;
import rsstats.roll.RollModifier;

import java.util.*;

/**
 * Менеджер модификаторов, позволяющий хранить, оперировать и извлекать модификаторы из NBT.
 * @author RareScrap
 */
public class ModifierManager { // TODO: UNIT-TEST ALL!

    private HashMap<IModifierDependent, List<RollModifier>> modifiers = new HashMap<IModifierDependent, List<RollModifier>>();

    /**
     * @return Модификаторы, увеличивающие или уменьшающие заданный параметр
     */
    public List<RollModifier> getModifiers(IModifierDependent target) {
        return modifiers.get(target);
    }

    /**
     * Добавляет модификатор(ы) к одному целевому параметру
     * @param target Параметр, для которого добавляются модификаторы
     * @param rollModifiers Модификатор(ы), которые нужно добавить для заданного праметра
     */
    public void addModifiers(IModifierDependent target, RollModifier... rollModifiers) {
        if (modifiers.get(target) == null)
            modifiers.put(target, new ArrayList<RollModifier>());

        modifiers.get(target).addAll( Arrays.asList(rollModifiers) );
    }

    /**
     * Добавляет по одному модификатору к разным целевым параметрам.
     * @param modifierDependentListMap Мапа, в которой ключ - целевой параметр, а значение - добавляемый модификатор
     */
    public void addModifiers(Map<IModifierDependent, RollModifier> modifierDependentListMap) {
        for (Map.Entry<IModifierDependent, RollModifier> entry : modifierDependentListMap.entrySet()) {
            addModifiers(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Удаляет модификатор(ы) из одного целевого параметра
     * @return true, если хоть один модификатор был удален. Иначе - false.
     */
    public boolean removeModifiers(IModifierDependent target, RollModifier... rollModifiers) { // Результат возвращается только тут, т.к. это по большому счету единственное место где он реально интересен
        List<RollModifier> targetModifiers = modifiers.get(target);
        if (targetModifiers == null) return false;

        boolean result = false;
        for (RollModifier rollModifier : rollModifiers)
            if (targetModifiers.remove(rollModifier)) result = true;

        if (targetModifiers.isEmpty()) modifiers.remove(target); // Если удалены все модификаторы - удалим сам список
        return result;
    }

    /**
     * Удаляет по одному подификатору, из разных целевых параметров
     * @param modifierDependentListMap Мапа, в которой ключ - целевой параметр, а значение - удаляемый модификатор
     * @return true, если хоть один модификатор был удален. Иначе - false.
     */
    public boolean removeModifiers(Map<IModifierDependent, RollModifier> modifierDependentListMap) {
        boolean result = false;
        for (Map.Entry<IModifierDependent, RollModifier> entry : modifierDependentListMap.entrySet()) {
            if ( removeModifiers(entry.getKey(), entry.getValue()) ) result = true;
        }
        return result;
    }

    /**
     * Вытаскивает модификаторы из стака
     * @param itemStack предмет с модификаторами
     * @return Мапа, в которой ключ - целевой параметр, а значение - модификатор
     */
    public Map<IModifierDependent, RollModifier> extractModifiersFrom(ItemStack itemStack) {
        HashMap<IModifierDependent, RollModifier> returnedMap = new HashMap<IModifierDependent, RollModifier>();

        if (itemStack != null && itemStack.getTagCompound() != null) { // извлекаем и сохраняем модификаторы
            NBTTagList modifiersList = itemStack.getTagCompound().getTagList("modifiers", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < modifiersList.tagCount(); i++) {
                NBTTagCompound modifierTag = modifiersList.getCompoundTagAt(i);

                int value = modifierTag.getInteger("value");
                String description = modifierTag.getString("description");
                String to = modifierTag.getString("to");
                RollModifier modifier = new RollModifier(value, description); // TODO: Замечен странный баг. При создании modifier с руским Description он создается нормально, но при входе в этот метод отладчиком все русские буквы из поля modifier.description удаляются! Как? Без понятия.

                // Пытаемся получить итем скилла или статы, к которому следует добавить модификатор
                StatItem item = (StatItem) GameRegistry.findItem(RSStats.MODID, to.substring(5)); // TODO: Это говно т.к. я юзаю unlocalizedName В качестве несуществующего на 1.7.10 registryName. Нужно перестать использовать unlocalizedName при регистрации итемов.
                if (item != null) {
                    returnedMap.put(item, modifier); // Добавляем модификатор к скиллу/стате
                } else {

                    // Если не нашли, пробуем достать параметр по этому имени
                    try {
                        ExtendedPlayer.ParamKeys paramKey = ExtendedPlayer.ParamKeys.valueOf(to);
                        returnedMap.put(paramKey, modifier); // Добавляем модификатор к параметрам
                    } catch (IllegalArgumentException e) {
                        e.initCause(new Throwable("Can't find stat, skill or player parameter with name \"" + to + "\""));
                        e.printStackTrace();
                    }

                }
            }
        }

        return returnedMap;
    }

    /**
     * Добавляет модификаторы, полученные из NBT стака
     * @param itemStack стак с модификаторами
     */
    public void addModifiersFrom(ItemStack itemStack) {
        addModifiers(extractModifiersFrom(itemStack));
    }

    /**
     * Удаляет из хранилища модификаторы, полученные из NBT стака
     * @param itemStack стак с модификаторами
     */
    public void removeModifiersFrom(ItemStack itemStack) {
        removeModifiers(extractModifiersFrom(itemStack));
    }

    /**
     * Полностью очищает список модификаторов
     */
    public void clear() {
        modifiers.clear();
    }
}
