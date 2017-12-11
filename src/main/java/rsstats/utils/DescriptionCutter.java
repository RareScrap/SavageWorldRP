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
}