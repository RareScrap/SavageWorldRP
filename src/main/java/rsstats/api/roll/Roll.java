package rsstats.api.roll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Базовый бросок дайсов
 */
public class Roll {
    /** Количество граней на кубике */
    public final int dice;
    /** Модификаторы, применяемые к броску */
    public List<RollModifier> modifiers = new ArrayList<RollModifier>();

    public boolean withWildDice = false;

    public Roll(int dice) {
        this.dice = dice;
    }

    public Roll(int dice, RollModifier... modifiers) {
        this(dice, Arrays.asList(modifiers));
    }

    public Roll(int dice, List<RollModifier> modifiers) {
        this.dice = dice;
        if (modifiers != null)
            this.modifiers.addAll(modifiers);
    }

    public Roll(Roll roll) {
        this(roll.dice, roll.modifiers); // TODO: Сделит Deep-copy? А зачем?
    }

    public Roll withWildDice(boolean flag) {
        this.withWildDice = flag;
        return this;
    }

    /**
     * Вычисляет бросок дайса
     */
    public Result roll() {
        if (dice <= 0)
            throw new RuntimeException("dice <= 0");

        // DEBUG
        //modificators = new ArrayList<RollModifier>();
        //modificators.add(new RollModifier(2, "Боевой дух"));
        //modificators.add(new RollModifier(-1, "Ранение"));
        //modificators.add(new RollModifier(-8, "Колдовской сглаз"));
        //modificators.add(new RollModifier(3, "Молитва стойкости"));

        // Подготовка объектов для генерации случайных чисел
        Random randomObject = new Random(); // Генератор случайных чисел
        Result result = new Result(modifiers);

        //int random; // Случайное число, заполняемое при каждой итерации цикла
        do {
            int random = randomObject.nextInt(dice)+1;
            result.mainRoll.add(random);

            if (random != dice) { // Выходим при первом невзырвном броске
                break;
            }
        } while (true);

        // Вычисляем бросок дикого кубика
        if (withWildDice) {
            do {
                int random = randomObject.nextInt(6) + 1; // TODO: Magic number
                result.wildRoll.add(random);

                if (random != 6) { // Выходим при первом невзырвном броске
                    break;
                }
            } while (true);
        }

        // Сформировать итоговое сообщение
        return result;
    }
}
