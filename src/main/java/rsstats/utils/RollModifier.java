package rsstats.utils;

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
            formatCode = "§" + RSStats.config.modifierColorPositive;
        } else {
            formatCode = "§" + RSStats.config.modifierColorNegative;
        }

        String[] words = description.split(" "); // Получаем слова из описания

        StringBuilder stringBuilder = new StringBuilder();
        for (String word : words) {
            // Присоединяем форматирование к каждому слову
            // TODO: Костыль. Стоит разобратся что за дичь творится с description, если убрать эту проверку
            if (word.charAt(0) != '§')
                stringBuilder.append(formatCode).append(word).append(" ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1); // Удаляем лишний пробел в последнем слове
        description = stringBuilder.toString(); // Сохраняем форматированное описание

        // Форматируем выходную строку
        return formatCode + "(" +
                formatCode + (value > 0 ? "+"+value : value) +
                formatCode + ": " +
                description +
                formatCode + ")" +
                "§" + RSStats.config.textColorNormal;
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
