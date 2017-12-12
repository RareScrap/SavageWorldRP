package rsstats.utils;

import java.util.List;
import java.util.Random;
import net.minecraft.util.StatCollector;

/**
 * Объект броска дайсов
 * @author RareScrap
 */
public class DiceRoll {
    /** Путь локализации для строки {@link #template} */
    private static final String ROLL_MESSAGE_LOCALE_KEY = "item.StatItem.rollChatMessage";
    
    /** Имя игрока, делающий бросок */
    private final String playerName;
    /** Имя пробрасываемой статы/навыка */
    private final String rollName;
    /** Количество граней на дайсе */
    private final int dice;
    /** Список модификаторов, которые должны быть учтены */
    private List<RollModifier> modificators;
    // TODO: Перенести локализацию строки-шаблона на сервер
    /** Строка-шаблон, в которую подставляются имена и результаты просков
     * ВНИМАНИЕ: эта строка не может локализироваться на строне сервера, т.к.
     * сервер всегда возвращает локаль en_EN. Мы вынуждены локализировать эту
     * строку на клиенте и передавать ее серверу. */
    private String template;
    
    /**
     * Клиентский конструктор, инициализирующий свои поля. Поле {@link #template}
     * берется из файлов локализации.
     * @param playerName Имя игрока, делающий бросок
     * @param rollName Имя пробрасываемой статы/навыка
     * @param dice Количество граней на дайсе
     */
    public DiceRoll(String playerName, String rollName, int dice) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.dice = dice;
        // Берется из файлов локализации
        this.template = StatCollector.translateToLocal(ROLL_MESSAGE_LOCALE_KEY);
    }
    
    /**
     * Клиентский конструктор, инициализирующий свои поля с уетом модификаторов
     * @param playerName Имя игрока, делающий бросок
     * @param rollName Имя пробрасываемой статы/навыка
     * @param dice Количество граней на дайсе
     * @param modificators Список модификаторов к броску
     */
    public DiceRoll(String playerName, String rollName, int dice, List<RollModifier> modificators) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.dice = dice;
        this.modificators = modificators;
        // Берется из файлов локализации
        this.template = StatCollector.translateToLocal(ROLL_MESSAGE_LOCALE_KEY);
    }
    
    /**
     * Серверный конструктор, инициализирующий свои поля. Нужен для того,
     * чтобы задать {@link #template} вручную, т.к. эту строку нельзя получить
     * через локализацию.
     * @param playerName Имя игрока, делающий бросок
     * @param rollName Имя пробрасываемой статы/навыка
     * @param dice Количество граней на дайсе
     * @param template Срока-шаблон для сообщеия броска
     */
    public DiceRoll(String playerName, String rollName, int dice, String template) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.dice = dice;
        this.template = template;
    }
    
    /**
     * Серверный конструктор, инициализирующий свои поля. Нужен для того,
     * чтобы задать {@link #template} вручную, т.к. эту строку нельзя получить
     * через локализацию.
     * @param playerName Имя игрока, делающий бросок
     * @param rollName Имя пробрасываемой статы/навыка
     * @param dice Количество граней на дайсе
     * @param modificators Список модификаторов к броску
     * @param template Срока-шаблон для сообщеия броска
     */
    public DiceRoll(String playerName, String rollName, int dice, List<RollModifier> modificators, String template) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.dice = dice;
        this.modificators = modificators;
        this.template = template;
    }
    
    // TODO: Базовые дайсы содержат только поле dice. Если потребуется переопределять базовые дайсы, в которых есть другие заполенные поля - создай похожий коструктор
    /**
     * Клиентский клонирующий конструктор, инициализирующий свои поля. Нужен для того,
     * чтобы определить броски для каждой статы/навыка на основе одного набора
     * базовых дайсов.
     * @param basicRoll Базовый дайс
     * @param playerName Имя игрока, делающий бросок
     * @param rollName Имя пробрасываемой статы/навыка
     * @param modificators Список модификаторов к броску
     */
    public DiceRoll(DiceRoll basicRoll, String playerName, String rollName, List<RollModifier> modificators) {
        this.playerName = playerName;
        this.rollName = rollName;
        this.dice = basicRoll.dice;
        this.modificators = modificators;
        this.template = StatCollector.translateToLocal(ROLL_MESSAGE_LOCALE_KEY);;
    }
    
    /**
     * Геттер для {@link #playerName}
     * @return Имя игрока, делающий бросок
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Геттер для {@link #rollName)
     * @return Имя пробрасываемой статы/навыка
     */
    public String getRollName() {
        return rollName;
    }
    
    /**
     * Геттер для {@link #dice)
     * @return Количество граней на дайсе
     */
    public int getDice() {
        return dice;
    }
    
    /**
     * Геттер для {@link #modificators)
     * @return Список модификаторов, которые должны быть учтены
     */
    public List<RollModifier> getModificators() {
        return modificators;
    }
    
    /**
     * Геттер для {@link #template)
     * @return Строка-шаблон
     */
    public String getTemplate() {
        return template;
    }
    
    
    
    /**
     * Вычисляет бросок
     * @return Сообщения броска
     */
    public String roll() {
        if (playerName == null)
            throw new NullPointerException("playerName is null");
        if (rollName == null)
            throw new NullPointerException("rollName is null");
        if (dice == 0)
            throw new NullPointerException("dice is 0");
        
        // Подготовка объектов для генерации случайных чисел
        Random randomObject = new Random(); // Генератор случайных чисел
        int rollResultInt = 0; // Сумма всех бросков при взрыве
        String rollResultString = ""; // Строка вида "4+4+4+2"

        int random; // Случайное число, заполняемое при каждой итерации цикла
        do {
            random = randomObject.nextInt(dice)+1;
            
            rollResultInt += random;
            rollResultString += random;

            // Обработка взрывных бросков
            if (random == dice) {
                rollResultString += "+";
            } else {
                rollResultString += "=" + rollResultInt;
                break;
            }
        } while (true);
        
        rollResultString += " ";

        // Обработка модификаторов
        if (modificators != null) {
            for (int i = 0; i < modificators.size(); ++i) {
                RollModifier rollModificator = modificators.get(i);

                /*rollResultInt += rollModificator.value;
                
                if (rollModificator.value >= 0)
                    rollResultString += "+";
                
                rollResultString += rollModificator.value + "("
                        + rollModificator.description + ") ";*/

                rollResultString += rollModificator.toString();
            }
        }

        // Сформировать итоговое сообщение
        return String.format(
                template,
                playerName,
                rollName,
                dice,
                rollResultString,
                rollResultInt
        );
                
                /*
                StatCollector.translateToLocalFormatted(
                "item.StatItem.rollChatMessage",
                playerName,
                rollName,
                dice,
                rollResultString,
                rollResultInt
        );
                */
    }
}
