package rsstats.roll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Результат броска
 * @author RareScrap
 */
public class Result {
    /**
     * Типы бросков
     */
    public enum RollType {
        MAIN, // Основной бросок
        WILD // Бросок дикого кубика
    }

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
     * Получить результат броска с учетом взрывных бросков
     * @param rollType Определяет для какого броска получить результат
     * @param withModifiers Учитывать ли модификаторы
     * @return суммарное количество очков броска
     */
    public int getTotal(RollType rollType, boolean withModifiers) {
        Integer total = 0;

        List<Integer> roll = null; // Определяем с каким броском работать
        switch (rollType) {
            case MAIN: roll = mainRoll; break;
            case WILD: roll = wildRoll; break;
        }

        for (Integer integer : roll) {
            total += integer;
        }
        if (withModifiers) // Учет модификаторов
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
     * Получить количество взрывов броска
     * @param rollType Определяет для какого броска получить результат
     * @return Число взрывов при броске
     */
    public int getUps(RollType rollType) {
        switch (rollType) {
            case MAIN: return mainRoll.size() - 1;
            case WILD: return wildRoll.size() - 1;
            default: throw new RuntimeException("Stub");
        }
    }

    /**
     * Определяет, использовался ли в броске дикий кубик
     */
    public boolean withWildDice() {
        return wildRoll.isEmpty();
    }

    /**
     * Возвращает читаемый результат броска
     * @param rollType Определяет для какого броска получить результат
     * @return Строка вида "6+6+6+3=21"
     */
    public String toString(RollType rollType) {
        StringBuffer strBuf = new StringBuffer();

        List<Integer> roll = null;
        switch (rollType) {
            case MAIN: roll = mainRoll; break;
            case WILD: roll = wildRoll; break;
        }

        for (int i = 0; i < roll.size(); i++) {
            strBuf.append(roll.get(i)).append("+");
        }
        strBuf.deleteCharAt(strBuf.length()-1); // Удаляем последний плюс
        strBuf.append("=").append(getTotal(rollType, false));

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

    /**
     * Возвращает бросок, который в наброл наибольшее количество очков в результате ролла.
     * @return Тип броска (см. {@link RollType})
     */
    public RollType getMax() {
        return getTotal(RollType.MAIN, false) > getTotal(RollType.WILD,false)
                ? RollType.MAIN
                : RollType.WILD;
    }
}
