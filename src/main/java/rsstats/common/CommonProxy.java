package rsstats.common;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import rsstats.common.event.KeyHandler;
import rsstats.common.network.*;
import rsstats.data.ExtendedPlayer;
import rsstats.inventory.container.MainContainer;
import rsstats.inventory.container.StatsContainer;
import rsstats.inventory.container.UpgradeContainer;
import rsstats.inventory.container.rsstats.blocks.UpgradeStationBlock;
import rsstats.inventory.container.rsstats.blocks.UpgradeStationEntity;
import rsstats.inventory.tabs_inventory.TabHostInventory;
import rsstats.inventory.tabs_inventory.TabMessageHandler;
import rsstats.items.ExpItem;
import rsstats.items.RerollCoin;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.utils.DescriptionCutter;
import rsstats.utils.DiceRoll;
import rsstats.utils.RollModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static rsstats.common.RSStats.instance;
import static rsstats.common.RSStats.proxy;

/**
 * Проки, содержащий код как для клиента, так и сервера
 * @author RareScrap
 */
public class CommonProxy implements IGuiHandler {
    public enum Skills {
        ClimbingSkillItem,
        EquitationSkillItem,
        LockpickingSkillItem,
        DrivingSkillItem,
        FightingSkillItem,
        DisguiseSkillItem,
        ThrowingSkillItem,
        PilotingSkillItem,
        SwimmingSkillItem,
        ShootingSkillItem,
        ShippingSkillItem,
        GamblingSkillItem,
        PerceptionSkillItem,
        SurvivalSkillItem,
        TrackingSkillItem,
        MedicineSkillItem,
        ProvocationSkillItem,
        InvestigationSkillItem,
        RepearSkillItem,
        StreetFlairSkillItem,
        IntimidationSkillItem,
        DiplomacySkillItem,
    }

    public enum Stats {
        StrengthStatItem,
        AgilityStatItem,
        IntelligenceStatItem,
        EnduranceStatItem,
        CharacterStatItem
    }

    public static class OtherItems {
        public static Item flawsTabItem = new OtherItem().setTextureName("rsstats:other_items/flawsTab").setUnlocalizedName("FlawsTabItem");
        public static Item perksTabItem = new OtherItem().setTextureName("rsstats:other_items/perksTab").setUnlocalizedName("PerksTabItem");
        public static Item positiveEffectsTabItem = new OtherItem().setTextureName("rsstats:other_items/positiveEffectsTab").setUnlocalizedName("PositiveEffectsTabItem");
        public static Item negativeEffectsTabItem = new OtherItem().setTextureName("rsstats:other_items/negativeEffectsTab").setUnlocalizedName("NegativeEffectsTabItem");

        public static class OtherItem extends Item {
            // Получаем информацию предмета из файла локализации
            @Override
            public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List infoLines, boolean p_77624_4_) {
                String[] strs = DescriptionCutter.cut(4, StatCollector.translateToLocal(getUnlocalizedName() + ".lore"));
                infoLines.addAll(Arrays.asList(strs));
            }
        }
    }

    /** Обработчик нажатия кнопок, используемых для вызова GUI */
    protected KeyHandler keyHandler;
    /** Обертка для работы с сетью */
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(RSStats.MODID.toLowerCase());

    public void preInit(FMLPreInitializationEvent event) {
        int discriminator = 0;

        // Когда сообщений станет много, их можно вынести в отдельный класс в метод init()
        INSTANCE.registerMessage(RollPacketToServer.MessageHandler.class, RollPacketToServer.class, discriminator++, Side.SERVER); // Регистрация сообщения о пробросе статы
        INSTANCE.registerMessage(PacketOpenRSStatsInventory.MessageHandler.class, PacketOpenRSStatsInventory.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenSSPPage.MessageHandler.class, PacketOpenSSPPage.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketOpenWindow.MessageHandler.class, PacketOpenWindow.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketShowSkillsByStat.MessageHandler.class, PacketShowSkillsByStat.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncGUI.MessageHandler.class, PacketSyncGUI.class, discriminator++, Side.SERVER);
        INSTANCE.registerMessage(PacketDialogAction.MessageHandler.class, PacketDialogAction.class, discriminator++, Side.SERVER);

        INSTANCE.registerMessage(PacketSyncPlayer.MessageHandler.class, PacketSyncPlayer.class, discriminator++, Side.CLIENT);
        INSTANCE.registerMessage(PacketCommandReponse.MessageHandler.class, PacketCommandReponse.class, discriminator++, Side.CLIENT);

        TabHostInventory.registerHandler(TabMessageHandler.class, discriminator++);
        // Класические спосб регистрации для дебага
        //CommonProxy.INSTANCE.registerMessage(TabMessageHandler.class, TabHostInventory.SetCurrentTabPacket.class, discriminator++, Side.SERVER);

        // Дайсы для статов
        ArrayList<DiceRoll> statDices = new ArrayList<DiceRoll>();
        statDices.add(new DiceRoll(null, null, 4));
        statDices.add(new DiceRoll(null, null, 6));
        statDices.add(new DiceRoll(null, null, 8));
        statDices.add(new DiceRoll(null, null, 10));
        statDices.add(new DiceRoll(null, null, 12));
        // Инициализация предметов статов
        StatItem strenghtStatItem = new StatItem(statDices, "StrengthStatItem", "rsstats:strenght", "item.StrengthStatItem"); // 3 - rarescrap:StrenghtIcon_
        StatItem agilityStatItem = new StatItem(statDices, "AgilityStatItem", "rsstats:agility", "item.AgilityStatItem");
        StatItem intelligenceStatItem = new StatItem(statDices, "IntelligenceStatItem", "rsstats:intelligence", "item.IntelligenceStatItem");
        StatItem enduranceStatItem = new StatItem(statDices, "EnduranceStatItem", "rsstats:endurance", "item.EnduranceStatItem");
        StatItem characterStatItem = new StatItem(statDices, "CharacterStatItem", "rsstats:character", "item.CharacterStatItem");
        // Регистрация предметов статов
        GameRegistry.registerItem(strenghtStatItem, "StrengthStatItem");
        GameRegistry.registerItem(agilityStatItem, "AgilityStatItem");
        GameRegistry.registerItem(intelligenceStatItem, "IntelligenceStatItem");
        GameRegistry.registerItem(enduranceStatItem, "EnduranceStatItem");
        GameRegistry.registerItem(characterStatItem, "CharacterStatItem");

        // Дайся для скиллов
        // TODO: Проверять на то, поставляется ли dices уже с модификаторами
        List<RollModifier> modificators = new ArrayList<RollModifier>();
        modificators.add(new RollModifier(-2, StatCollector.translateToLocal("modifiers.MissingSkill"))); // TODO: Разобраться почему это работает на сервере
        ArrayList<DiceRoll> skillDices = new ArrayList<DiceRoll>();
        skillDices.add(new DiceRoll(null, null, 4, modificators)); // Создание дополнительного броска для нулевого уровня скиллов
        skillDices.add(new DiceRoll(null, null, 4));
        skillDices.add(new DiceRoll(null, null, 6));
        skillDices.add(new DiceRoll(null, null, 8));
        skillDices.add(new DiceRoll(null, null, 10));
        skillDices.add(new DiceRoll(null, null, 12));
        // Инициализация предметов склиллов
        SkillItem equitationSkillItem = new SkillItem(skillDices, "EquitationSkillItem", "rsstats:skills/Equitation", "item.EquitationSkillItem", agilityStatItem);
        SkillItem lockpickingSkillItem = new SkillItem(skillDices, "LockpickingSkillItem", "rsstats:skills/Lockpicking", "item.LockpickingSkillItem", agilityStatItem);
        SkillItem drivingSkillItem = new SkillItem(skillDices, "DrivingSkillItem", "rsstats:skills/Driving", "item.DrivingSkillItem", agilityStatItem);
        SkillItem fightingSkillItem = new SkillItem(skillDices, "FightingSkillItem", "rsstats:skills/Fighting", "item.FightingSkillItem", agilityStatItem);
        SkillItem disguiseSkillItem = new SkillItem(skillDices, "DisguiseSkillItem", "rsstats:skills/Disguise", "item.DisguiseSkillItem", agilityStatItem);
        SkillItem throwingSkillItem = new SkillItem(skillDices, "ThrowingSkillItem", "rsstats:skills/Throwing", "item.ThrowingSkillItem", agilityStatItem);
        SkillItem pilotingSkillItem = new SkillItem(skillDices, "PilotingSkillItem", "rsstats:skills/Piloting", "item.PilotingSkillItem", agilityStatItem);
        SkillItem swimmingSkillItem = new SkillItem(skillDices, "SwimmingSkillItem", "rsstats:skills/Swimming", "item.SwimmingSkillItem", agilityStatItem);
        SkillItem shootingSkillItem = new SkillItem(skillDices, "ShootingSkillItem", "rsstats:skills/Shooting", "item.ShootingSkillItem", agilityStatItem);
        SkillItem shippingSkillItem = new SkillItem(skillDices, "ShippingSkillItem", "rsstats:skills/Shipping", "item.ShippingSkillItem", agilityStatItem);
        SkillItem gamblingSkillItem = new SkillItem(skillDices, "GamblingSkillItem", "rsstats:skills/Gambling", "item.GamblingSkillItem", intelligenceStatItem);
        SkillItem perceptionSkillItem = new SkillItem(skillDices, "PerceptionSkillItem", "rsstats:skills/Perception", "item.PerceptionSkillItem", intelligenceStatItem);
        SkillItem survivalSkillItem = new SkillItem(skillDices, "SurvivalSkillItem", "rsstats:skills/Survival", "item.SurvivalSkillItem", intelligenceStatItem);
        SkillItem trackingSkillItem = new SkillItem(skillDices, "TrackingSkillItem", "rsstats:skills/Tracking", "item.TrackingSkillItem", intelligenceStatItem);
        SkillItem medicineSkillItem = new SkillItem(skillDices, "MedicineSkillItem", "rsstats:skills/Medicine", "item.MedicineSkillItem", intelligenceStatItem);
        SkillItem provocationSkillItem = new SkillItem(skillDices, "ProvocationSkillItem", "rsstats:skills/Provocation", "item.ProvocationSkillItem", intelligenceStatItem);
        SkillItem investigationSkillItem = new SkillItem(skillDices, "InvestigationSkillItem", "rsstats:skills/Investigation", "item.InvestigationSkillItem", intelligenceStatItem);
        SkillItem repearSkillItem = new SkillItem(skillDices, "RepearSkillItem", "rsstats:skills/Repear", "item.RepearSkillItem", intelligenceStatItem);
        SkillItem streetFlairSkillItem = new SkillItem(skillDices, "StreetFlairSkillItem", "rsstats:skills/StreetFlair", "item.StreetFlairSkillItem", intelligenceStatItem);
        SkillItem intimidationSkillItem = new SkillItem(skillDices, "IntimidationSkillItem", "rsstats:skills/Intimidation", "item.IntimidationSkillItem", characterStatItem);
        SkillItem diplomacySkillItem = new SkillItem(skillDices, "DiplomacySkillItem", "rsstats:skills/Diplomacy", "item.DiplomacySkillItem", characterStatItem);
        SkillItem climbingSkillItem = new SkillItem(skillDices, "ClimbingSkillItem", "rsstats:skills/Climbing", "item.ClimbingSkillItem", strenghtStatItem);
        // Регистрация предметов скиллов
        GameRegistry.registerItem(equitationSkillItem, "EquitationSkillItem");
        GameRegistry.registerItem(lockpickingSkillItem, "LockpickingSkillItem");
        GameRegistry.registerItem(drivingSkillItem, "DrivingSkillItem");
        GameRegistry.registerItem(fightingSkillItem, "FightingSkillItem");
        GameRegistry.registerItem(disguiseSkillItem, "DisguiseSkillItem");
        GameRegistry.registerItem(throwingSkillItem, "ThrowingSkillItem");
        GameRegistry.registerItem(pilotingSkillItem, "PilotingSkillItem");
        GameRegistry.registerItem(swimmingSkillItem, "SwimmingSkillItem");
        GameRegistry.registerItem(shootingSkillItem, "ShootingSkillItem");
        GameRegistry.registerItem(shippingSkillItem, "ShippingSkillItem");
        GameRegistry.registerItem(gamblingSkillItem, "GamblingSkillItem");
        GameRegistry.registerItem(perceptionSkillItem, "PerceptionSkillItem");
        GameRegistry.registerItem(survivalSkillItem, "SurvivalSkillItem");
        GameRegistry.registerItem(trackingSkillItem, "TrackingSkillItem");
        GameRegistry.registerItem(medicineSkillItem, "MedicineSkillItem");
        GameRegistry.registerItem(provocationSkillItem, "ProvocationSkillItem");
        GameRegistry.registerItem(investigationSkillItem, "InvestigationSkillItem");
        GameRegistry.registerItem(repearSkillItem, "RepearSkillItem");
        GameRegistry.registerItem(streetFlairSkillItem, "StreetFlairSkillItem");
        GameRegistry.registerItem(intimidationSkillItem, "IntimidationSkillItem");
        GameRegistry.registerItem(diplomacySkillItem, "DiplomacySkillItem");
        GameRegistry.registerItem(climbingSkillItem, "ClimbingSkillItem");

        GameRegistry.registerItem(OtherItems.perksTabItem, OtherItems.perksTabItem.getUnlocalizedName());
        GameRegistry.registerItem(OtherItems.flawsTabItem, OtherItems.flawsTabItem.getUnlocalizedName());
        GameRegistry.registerItem(OtherItems.positiveEffectsTabItem, OtherItems.positiveEffectsTabItem.getUnlocalizedName());
        GameRegistry.registerItem(OtherItems.negativeEffectsTabItem, OtherItems.negativeEffectsTabItem.getUnlocalizedName());

        // Регистрация прочих предметов
        RerollCoin rerollCoinItem = new RerollCoin("RerollCoinItem");
        GameRegistry.registerItem(rerollCoinItem, "RerollCoinItem");
        ExpItem expItem = new ExpItem("ExpItem");
        GameRegistry.registerItem(expItem, "ExpItem");

        // Регистрация сущностей
        GameRegistry.registerTileEntity(UpgradeStationEntity.class, "UpgradeStationEntity");

        /// регистрация блоков
        //UpgradeStationBlock b = new UpgradeStationBlock(); // TODO: Нужно найти способ снова использовать локальне переменные. Или хотя бы узнать, почему их не следует использовать

        /* Нет смысла регистрировать Item для блока. Потому что они регаются автоматические
         * при регистрации самого блока. Попытка сделать это еще раз привет к ошибке того,
         * что регитсрационный слот под итем уже занят
         */
        //GameRegistry.registerItem(new ItemBlock(b), UpgradeStationBlock.item_name);

        GameRegistry.registerBlock(b, UpgradeStationBlock.name);


        // Это не срабатывает. Скорее всего, это решение предназначено для более поздних версий Forge
        /*UpgradeStationBlock block3DWeb = (Block3DWeb)(new Block3DWeb().setUnlocalizedName("mbe05_block_3d_web_unlocalised_name"));
        block3DWeb.setRegistryName("mbe05_block_3d_web_registry_name");
        ForgeRegistries.BLOCKS.register(block3DWeb);

        // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
        ItemBlock itemBlock3DWeb = new ItemBlock(block3DWeb);
        itemBlock3DWeb.setRegistryName(block3DWeb.getRegistryName());
        ForgeRegistries.ITEMS.register(itemBlock3DWeb);*/
    }
    // TODO: Это статик поле - полный пиздец как по мне, но без него не зарегать ItemRender в ClientProxy для этого блока
    public static UpgradeStationBlock  b = new UpgradeStationBlock();

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        registerKeyBindings();
    }

    public void postInit(FMLPostInitializationEvent event) {}

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case RSStats.GUI:
                return new MainContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory, ExtendedPlayer.get(player).skillsInventory, ExtendedPlayer.get(player).wearableInventory, ExtendedPlayer.get(player).otherTabsHost, ExtendedPlayer.get(player).otherTabsInventory);
            case RSStats.SSP_UI_CODE:
                return new StatsContainer(player, player.inventory, ExtendedPlayer.get(player).statsInventory);
            case RSStats.UPGRADE_UI_FROM_BLOCK_CODE: {
                // Получение сущности по координатам блока, по которому кликнул игрок
                TileEntity tileEntity = world.getTileEntity(x, y, z);
                if (tileEntity instanceof UpgradeStationEntity) {
                    UpgradeStationEntity upgradeStationEntity = (UpgradeStationEntity) tileEntity;
                    return new UpgradeContainer(player.inventory, upgradeStationEntity.upgradeStationInventory);
                }
                break;
            }
            case RSStats.UPGRADE_UI_FROM_CMD_CODE:
                return new UpgradeContainer(player.inventory, null);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null; // Переопределяется в ClientProxy
    }
    
    // Переопределяется в ClientProxy
    public void registerKeyBindings() {}
}
