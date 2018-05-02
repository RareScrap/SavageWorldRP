package rsstats.utils;

import net.minecraft.util.StatCollector;
import rsstats.common.RSStats;

/**
 * Модификатора броска
 * @author RareScrap
 */
public class RollModifier {
    /** Значение модификатора */
    private int value;
    /** Описание модификатора */
    private String description;

    /**
     * Конструктор инициализирующий свои поля
     * @param value Значение модификатора
     * @param description Описание модификатора
     */
    public RollModifier(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Вывод модификатора в формате: (+4: За наличие интрументов)
     * @return Строкое представление модификатора
     */
    @Override
    public String toString() {
        String formatCode;
        if (value > 0) {
            formatCode = RSStats.config.modifierColorPositive;
        } else {
            formatCode = RSStats.config.modifierColorNegative;
        }

        // TODO: Из-за бага с русскими буквами в RollModifier.description, может придти пустое слово. Все работает норм, но при юзании отладчика - можно увидеть как сюда приходит пустое слово. В игре такое не разу не замечено
        // TODO: Костыль. Стоит разобратся что за дичь творится с description, если убрать эту проверку на § в этой функции
        description = DescriptionCutter.formatEveryWord(description, "\u00A7" + formatCode); // Символ §

        // Форматируем выходную строку
        return StatCollector.translateToLocalFormatted("modifier.string", value > 0 ? "+"+value : String.valueOf(value), description, formatCode);
    }

    /**
     * Геттер для {@link #value}
     * @return Значение модификатора
     */
    public int getValue() {
        return value;
    }

    /**
     * Геттер для {@link #description}
     * @return Описание модификатора
     */
    public String getDescription() {
        return description;
    }
}