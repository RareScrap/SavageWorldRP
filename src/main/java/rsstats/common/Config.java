package rsstats.common;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

// TODO: Дочитай туториал в MinecraftByExample

/**
 * Синглтон-класс, управляющий конфигурацией мода.
 */
public class Config {
    /** Числовой код цвета позитивных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_POSITIVE = "2";
    /** Числовой код цвета негативных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_NEGATIVE = "4";

    private static final String CATEGORY_CHAT = "category_chat";
    private static final String TEXT_COLOR_NORMAL_KEY = "text_color_normal";
    private static final String MODIFIER_COLOR_POSITIVE_KEY = "modifier_color_positive";
    private static final String MODIFIER_COLOR_NEGATIVE_KEY = "modifier_color_negative";

    /** Инферфейс управления конфигурацией */
    private Configuration configuration;

    public String textColorNormal;
    public String modifierColorPositive;
    public String modifierColorNegative;

    private static Config config;

    private Config(File configFile) {
        configuration = new Configuration(configFile);
        configuration.load();
        save();
    }

    public static Config getConfig(File configFile) {
        if (config == null) {
            config = new Config(configFile);
        }

        return config;
    }

    public void save() {
        Property propertyTextColorNormal = configuration.get(CATEGORY_CHAT, TEXT_COLOR_NORMAL_KEY, "f");
        Property propertyModifierColorPositive = configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_POSITIVE_KEY, DEFAULT_MODIFIER_COLOR_POSITIVE);//new Property(MODIFIER_COLOR_POSITIVE_KEY, 2, Type.CHAR_TYPE);
        Property propertyModifierColorNegative = configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_NEGATIVE_KEY, DEFAULT_MODIFIER_COLOR_NEGATIVE);

        textColorNormal = propertyTextColorNormal.getString();
        modifierColorPositive = propertyModifierColorPositive.getString();
        modifierColorNegative = propertyModifierColorNegative.getString();

        configuration.save();
    }
}
