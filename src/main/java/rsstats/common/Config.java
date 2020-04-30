package rsstats.common;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;
import static rsstats.common.RSStats.MODID;
import static rsstats.common.RSStats.MODNAME;
import static rsstats.utils.Utils.getChatFormattingFromCode;

// TODO: Дочитай туториал в MinecraftByExample

/**
 * Синглтон-класс, управляющий конфигурацией мода.
 */
public class Config {
    /** Числовой код цвета позитивных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_POSITIVE = String.valueOf(EnumChatFormatting.GREEN.getFormattingCode());
    /** Числовой код цвета негативных модификаторов по умолчанию. Представлен строкой. */
    public static final String DEFAULT_MODIFIER_COLOR_NEGATIVE = String.valueOf(EnumChatFormatting.RED.getFormattingCode());
    public static final boolean DEFAULT_IGNORE_DOWNTIME_IN_COOLDOWN = false;

    private static final String CATEGORY_CHAT = "client chat";

    private static final String MODIFIER_COLOR_POSITIVE_KEY = "modifierColorPositive";
    private static final String MODIFIER_COLOR_NEGATIVE_KEY = "modifierColorNegative";
    private static final String IGNORE_DOWNTIME_IN_COOLDOWN_KEY = "ignoreDowntimeInCooldown";

    /** Инферфейс управления конфигурацией */
    private Configuration configuration;

    public EnumChatFormatting modifierColorPositive;
    public EnumChatFormatting modifierColorNegative;
    public boolean ignoreDowntimeInCooldown;

    private static Config config;

    private Config() {
        File configFile = new File(Loader.instance().getConfigDir(), MODNAME+".cfg");
        configuration = new Configuration(configFile);
    }

    public static Config getConfig() { // TODO: ну стоит ли так усложнять? Все равно на это похуй всем
        if (config == null) {
            config = new Config();
            config.syncConfig(true);
        }

        return config;
    }

    /**
     * Synchronizes the local fields with the values in the Configuration object.
     */
    public void syncConfig(boolean load)
    {
        // By adding a property order list we are defining the order that the properties will appear both in the config file and on the GUIs.
        // Property order lists are defined per-ConfigCategory.
        List<String> propOrder = new ArrayList<String>();

        if (!configuration.isChild)
        {
            if (load)
            {
                configuration.load();
            }
            // TODO: Что это?
//            Property enableGlobalCfg = configuration.get(Configuration.CATEGORY_GENERAL, "enableGlobalConfig", false).setShowInGui(false);
//            if (enableGlobalCfg.getBoolean(false))
//            {
//                Configuration.enableGlobalConfig();
//            }
        }

        Property prop;

        prop = configuration.get(CATEGORY_GENERAL, IGNORE_DOWNTIME_IN_COOLDOWN_KEY, DEFAULT_IGNORE_DOWNTIME_IN_COOLDOWN);
        ignoreDowntimeInCooldown = prop.getBoolean();
        propOrder.add(prop.getName());

        prop = configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_POSITIVE_KEY, DEFAULT_MODIFIER_COLOR_POSITIVE, "Цвет положительных модификаторов броска", Property.Type.COLOR);
        prop.setValidValues(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"});
        setLanguageKey(prop, MODIFIER_COLOR_POSITIVE_KEY);
        modifierColorPositive = getChatFormattingFromCode(prop.getString());
        propOrder.add(prop.getName());

        prop = configuration.get(CATEGORY_CHAT, MODIFIER_COLOR_NEGATIVE_KEY, DEFAULT_MODIFIER_COLOR_NEGATIVE, "Цвет отрицательных модификаторов броска", Property.Type.COLOR);
        prop.setValidValues(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"});
        setLanguageKey(prop, MODIFIER_COLOR_NEGATIVE_KEY);
        modifierColorNegative = getChatFormattingFromCode(prop.getString());
        propOrder.add(prop.getName());

        configuration.setCategoryPropertyOrder(Config.CATEGORY_CHAT, propOrder);

        if (configuration.hasChanged())
        {
            configuration.save();
        }
    }

    private static void setLanguageKey(Property property, String propertyKey) {
        property.setLanguageKey(MODID + ".configgui." + propertyKey);
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
        public TestModConfigGUI(GuiScreen parent) { // TODO: черный цвет под кодом "0" не отображается на кнопке
            super(parent,
                    new ConfigElement(RSStats.config.configuration.getCategory(CATEGORY_CHAT)).getChildElements(), // TODO: добавить категорию чата
                    RSStats.MODID, false, false, GuiConfig.getAbridgedConfigPath(RSStats.config.configuration.toString()));

            // TODO: Сменить пример текста на свой в ChatColorEntry
        }
    }
}
