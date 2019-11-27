package rsstats.roll;

import net.minecraft.util.StatCollector;
import rsstats.i18n.IClientTranslatable;

/**
 * Модификатора броска
 * @author RareScrap
 */
public class RollModifier implements IClientTranslatable {
    /* Object#equals(Object) не должен быть зависимым от mutable-полей, т.к. это приводит к плохим пследствиям:
     * https://www.artima.com/lejava/articles/equality.html Pitfall #3
     * Именно поэтому эти поля final. */
    /** Значение модификатора */
    public final int value;
    /** Описание модификатора */
    public final String description;

    /**
     * Конструктор инициализирующий свои поля
     * @param value Значение модификатора
     * @param description Описание модификатора
     */
    public RollModifier(int value, String description) { // TODO: Как-то добавить возможость задать i18n модификаторам
        this.value = value;

        // Мне не нужен null в description
        if (description != null) // Из-за language level 6 я не могу использовать @NutNull, так что пусть будет так.
            this.description = description;
        else
            this.description = "";
    }

    /**
     * Вывод модификатора в формате: (+4: За наличие интрументов)
     * @return Строкое представление модификатора
     */
    @Override
    public String toString() {
        return "(" + (value > 0 ? "+"+value : value) + ": " + description + ")";
    }

    @Override
    public String getTranslatedString() {
        String localizedDescription = StatCollector.translateToLocal(description);

        // Форматируем выходную строку
        return StatCollector.translateToLocalFormatted(
                "modifier.string",
                value > 0 ? "+"+value : String.valueOf(value),
                localizedDescription == null ? description : localizedDescription);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RollModifier) {
            RollModifier modifier = (RollModifier) obj;
            return value == modifier.value && description.equals(modifier.description);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // Гугл говорит, что стандартный способ оверрайда хэша на java6 и меньше
        // TODO: Разобраться
        int result = 17; // Я точно не знаю зачем это нужно, но гугл говорит что из книги "Effective Java"
        result = 31 * result + value;
        result = 31 * result + description.hashCode();
        return result;
    }
}