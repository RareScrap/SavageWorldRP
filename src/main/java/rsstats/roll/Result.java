package rsstats.roll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Результат броска
 * @author RareScrap
 */
public class Result {
    /** Бросок основного навыка */
    public List<Integer> mainRoll = new ArrayList<Integer>();
    /** Бросок дикого кубика */
    public List<Integer> wildRoll = new ArrayList<Integer>();
    /** Модификаторы, применяемые к броску */
    public List<RollModifier> modifiers;

    /**
     * Конструктор. Опционально можно задать модификаторы.
     */
    public Result(RollModifier... modifiers) {
        this.modifiers = new ArrayList<RollModifier>(Arrays.asList(modifiers)); // TODO: Протестить
    }

    /**
     * Альтернативный конструктор, задающий модификаторы списком.
     * @param modifiers Список модификаторов
     */
    public Result(List<RollModifier> modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * @param withModifiers Учитывать ли модификаторы
     * @return Сумма основного броска
     */
    public int getMainTotal(boolean withModifiers) {
        Integer total = 0;
        for (Integer integer : mainRoll) {
            total += integer;
        }
        if (withModifiers)
            total += getModifiersSum();
        return total;
    }

    /**
     * @param withModifiers Учитывать ли модификаторы
     * @return Сумма броска дикого кубика
     */
    public int getWildTotal(boolean withModifiers) {
        Integer total = 0;
        for (Integer integer : wildRoll) {
            total += integer;
        }
        if (withModifiers)
            total += getModifiersSum();
        return total;
    }

    /**
     * @return Сумма всех модификаторов броска
     */
    public int getModifiersSum() {
        int modifiersSum = 0;
        for (RollModifier modifier : this.modifiers) {
            modifiersSum += modifier.getValue();
        }
        return modifiersSum;
    }

    /**
     * @return Количество взрывов основного броска
     */
    public int getMainUps() {
        return mainRoll.size() - 1;
    }

    /**
     * @return Количество взрывов броска дикого кубика
     */
    public int getWildUps() {
        if (withWildDice()) {
            return wildRoll.size() - 1;
        } else {
            return 0;
        }
    }

    /**
     * Определяет, использовался ли в броске дикий кубик
     */
    public boolean withWildDice() {
        return wildRoll.isEmpty();
    }

    /**
     * Возвращает читаемый результат основного броска
     * @return Строка вида "6+6+6+3=21"
     */
    public String mainToString() {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < mainRoll.size(); i++) {
            strBuf.append(mainRoll.get(i)).append("+");
        }
        strBuf.deleteCharAt(strBuf.length()-1); // Удаляем последний плюс
        strBuf.append("=").append(getMainTotal(false));
        return strBuf.toString();
    }

    /**
     * Возвращает читаемый результат броска дикого кубика
     * @return Строка вида "6+6+6+3=21"
     */
    public String wildToString() {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < wildRoll.size(); i++) {
            strBuf.append(wildRoll.get(i)).append("+");
        }
        strBuf.deleteCharAt(strBuf.length()-1); // Удаляем последний плюс
        strBuf.append("=").append(getWildTotal(false));
        return strBuf.toString();
    }

    /**
     * Возвращает читаемыю строку модификаторов
     * @return Строка вида "(+2: Доспехи ловкости) (-3: Проклятие слабоумия) ..."
     */
    public String modifiersToString() {
        StringBuffer strBuf = new StringBuffer();
        for (RollModifier modifier : modifiers) {
            strBuf.append(modifier.toString()).append(" ");
        }
        strBuf.trimToSize();
        return strBuf.toString();
    }
}
