package rsstats.utils;

/**
 * Класс-утилита для деленния строки на несколько строк
 * @author RareScrap
 */
public class DescriptionCutter {
    /**
     * Делит входящую строку на нескольско строк по несколько слов в каждой
     * @param numberOfWords Количетсов слов в каждой строке
     * @param sourceString Исходная строка
     * @return Массив строк с numberOfWords слов в каждой
     */
    public static String[] cut(int numberOfWords, String sourceString) {
        // Добавляем пробел к концу строки, т.к. мы считаем, что в строке столько слов, сколько в ней пробелов
        sourceString = sourceString + " ";
        String[] words = sourceString.split(" "); // Получаем массив слов

        // Определдяем размер массива возвращаемых слов
        String[] returnStrings = new String[
                (int) java.lang.Math.ceil( sourceString.split(" ").length / (double) numberOfWords )
                ];

        // Пробегаемся по sourceString и заполняем массив строк
        for (int wordIndex = 0, stringIndex = 0; wordIndex < words.length; stringIndex++) {
            returnStrings[stringIndex] = ""; // Ициниализируем строку пустой строкой
            for (int substringLimit = wordIndex+numberOfWords; wordIndex < substringLimit && wordIndex < words.length; wordIndex++) {
                returnStrings[stringIndex] += words[wordIndex] + " ";
            }
        }

        // Возвращем получившиеся строки
        return returnStrings;
    }

    /**
     * Присоединяет к каждому слову в строке форматирование, если слово его не имеет
     * @param source Исходная строка
     * @param formatPrefix Форматирование, которое будет присоединено к каждому слову
     * @return Строка с форматированными словами
     */
    public static String formatEveryWord(String source, String formatPrefix ) {
        String[] words = source.split(" "); // Получаем слова

        // Присоединяем форматирование к каждому слову, если оно отсуствует
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) break; // Защита от пустых слов

            if (word.charAt(0) != '§')
                stringBuilder.append(formatPrefix).append(word);

            // Предотвращаем наличие пробела в конце последнего слова
            if (i != words.length-1) {
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();

    }
}