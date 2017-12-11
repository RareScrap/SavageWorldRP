package rsstats.utils;

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
        return "(" + value +": " + description + ")";
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
