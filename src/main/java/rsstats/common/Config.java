package rsstats.common;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Set;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

// TODO: Дочитай туториал в MinecraftByExample

/**
 * Синглтон-класс, управляющий конфигурацией мода.
 */
public class Config {
    /** Числовой код цвета позитивных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_POSITIVE = "green";
    /** Числовой код цвета негативных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_NEGATIVE = "red";
    public static final boolean DEFAULT_IGNORE_DOWNTIME_IN_COOLDOWN = false;

    private static final String CATEGORY_CHAT = "client chat";

    private static final String TEXT_COLOR_NORMAL_KEY = "textColorNormal";
    private static final String MODIFIER_COLOR_POSITIVE_KEY = "modifierColorPositive";
    private static final String MODIFIER_COLOR_NEGATIVE_KEY = "modifierColorNegative";
    private static final String IGNORE_DOWNTIME_IN_COOLDOWN_KEY = "ignoreDowntimeInCooldown";

    /** Инферфейс управления конфигурацией */
    private Configuration configuration;

    public String textColorNormal;
    public EnumChatFormatting modifierColorPositive;
    public EnumChatFormatting modifierColorNegative;

    public boolean ignoreDowntimeInCooldown;

    private static Config config;

    private Config(File configFile) {
        configuration = new Configuration(configFile);
        load();
        save();
    }

    public static Config getConfig(File configFile) {
        if (config == null) {
            config = new Config(configFile);
        }

        return config;
    }

    public void save() {
        // CATEGORY_GENERAL
        configuration.getBoolean(IGNORE_DOWNTIME_IN_COOLDOWN_KEY, CATEGORY_GENERAL, DEFAULT_IGNORE_DOWNTIME_IN_COOLDOWN, "Стоит ли кулдаунам игнорировать время, которое сервер провел в выключенном состоянии");

        // CHAT_CLIENT
        configuration.getString(TEXT_COLOR_NORMAL_KEY, CATEGORY_CHAT, "f", "test comment");
        configuration.getString(MODIFIER_COLOR_POSITIVE_KEY, CATEGORY_CHAT, DEFAULT_MODIFIER_COLOR_POSITIVE, "Цвет положительный модификаторов броска");//new Property(MODIFIER_COLOR_POSITIVE_KEY, 2, Type.CHAR_TYPE);
        configuration.getString(MODIFIER_COLOR_NEGATIVE_KEY, CATEGORY_CHAT, DEFAULT_MODIFIER_COLOR_NEGATIVE, "Цвет отрицательных модификаторов броска");

        configuration.save();
    }

    public void load() {
        configuration.load();
        ignoreDowntimeInCooldown = configuration.get(CATEGORY_GENERAL, IGNORE_DOWNTIME_IN_COOLDOWN_KEY, DEFAULT_IGNORE_DOWNTIME_IN_COOLDOWN, "test comment").getBoolean();

        textColorNormal = configuration.get(CATEGORY_CHAT, TEXT_COLOR_NORMAL_KEY, "f").getString();
        modifierColorPositive = EnumChatFormatting.getValueByName(configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_POSITIVE_KEY, DEFAULT_MODIFIER_COLOR_POSITIVE).getString());//new Property(MODIFIER_COLOR_POSITIVE_KEY, 2, Type.CHAR_TYPE);
        modifierColorNegative = EnumChatFormatting.getValueByName(configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_NEGATIVE_KEY, DEFAULT_MODIFIER_COLOR_NEGATIVE).getString());


    }

    public static class GuiFactory implements IModGuiFactory {

        @Override
        public void initialize(Minecraft minecraftInstance) {

        }

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return TestModConfigGUI.class;
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return null;
        }

        @Override
        public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
            return null;
        }
    }

    public static class TestModConfigGUI extends GuiConfig {
        public TestModConfigGUI(GuiScreen parent) {
            super(parent,
                    new ConfigElement(RSStats.config.configuration.getCategory(CATEGORY_GENERAL)).getChildElements(),
                    "TestMod", false, false, GuiConfig.getAbridgedConfigPath(RSStats.config.configuration.toString()));
        }
    }
}
