package rsstats.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketSyncPlayer;
import rsstats.i18n.IClientTranslatable;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.inventory.container.MainContainer;
import rsstats.items.OtherItems;
import rsstats.items.SkillItems;
import rsstats.items.StatItem;
import rsstats.items.StatItems;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.utils.Utils;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

/**
 *
 * @author rares
 */
public class ExtendedPlayer implements IExtendedEntityProperties {

    public enum Rank implements IClientTranslatable {
        NOVICE,
        TEMPERED,
        VETERAN,
        HERO,
        LEGEND;

        public int toInt() {
            return this.ordinal();
        }

        public static Rank fromInt(int lvl) {
            return Rank.values()[lvl];
        }

        @Override
        public String getTranslatedString() {
            return StatCollector.translateToLocal("rank." + this.name().toLowerCase());
        }
    }

    /** Ключи статичных параметров игрока */
    public enum ParamKeys implements IModifierDependent {
        STEP,
        PROTECTION,
        PERSISTENCE,
        CHARISMA,
        TIREDNESS,
        TIREDNESS_LIMIT
    }

    /** Каждый наследник {@link IExtendedEntityProperties} должен иметь индивидуальное имя */
    private static final String EXTENDED_ENTITY_TAG = RSStats.MODID;

    private final EntityPlayer entityPlayer;

    /** Основной параметр игрока - Шаг */
    public int step = 6;
    /** Основной параметр игрока - Защита */
    public int protection;
    /** Основной параметр игрока - Стойкость */
    public int persistence;
    /** Основной параметр игрока - Харизма */
    public int charisma = 0;

    /** Вторичный параметр игрока - Усталость */
    public int tiredness = 0;
    /** Вторичный параметр игрока - Предел усталость */
    public int tirednessLimit = 25;

    public int exp = 0;
    public Rank rank;

    /** Контейнер инвентаря, который будет синхронизироваться с клиентом даже тогда, когда не открыт. Прямо как
     * {@link EntityPlayer#inventoryContainer}. Именно через этот контейнер и будут синнхронизироваться инвентари. */
    public MainContainer mainContainer;

    /** Инвентарь для статов */
    public final StatsInventory statsInventory;
    /** Инвентарь для скиллов */
    public final SkillsInventory skillsInventory;
    /** Инвентарь для носимых предметов */
    public final WearableInventory wearableInventory;
    /** Хост вкладок для {@link #otherTabsInventory} */
    public TabHostInventory otherTabsHost;
    /** Инвентарь с вкладками для прочей информации вроде перков, изъянов и т.д. */
    public TabInventory otherTabsInventory;

    /** Менеджер прокачки, содержащий правила развития персонажа */
    public LevelupManager levelupManager; // Server thread only
    /** Хранилище модификаторов броска игрока */
    public ModifierManager modifierManager = new ModifierManager();

    /*
    Тут в виде полей можно хранить дополнительную информацию о Entity: мана,
    золото, хп, переносимый вес, уровень радиации, репутацию и т.д. Т.е. все то,
    что нельзя хранить в виде блоков
    */

    private ExtendedPlayer(EntityPlayer player) {
        this.entityPlayer = player;
        wearableInventory = new WearableInventory(this);

        statsInventory = new StatsInventory("stats_inv", 9);
        skillsInventory = new SkillsInventory("skills_inv", 36, entityPlayer, statsInventory);

        otherTabsHost = new TabHostInventory("effects_host", 4);
        otherTabsInventory = new TabInventory("effects", 36, entityPlayer, otherTabsHost);
    }
    
    /**
     * Used to register these extended properties for the entityPlayer during EntityConstructing event
     * This method is for convenience only; it will make your code look nicer
     * @param player
     */
    public static final void register(EntityPlayer player) {
        player.registerExtendedProperties(ExtendedPlayer.EXTENDED_ENTITY_TAG, new ExtendedPlayer(player));
    }
    
    /**
     * Returns ExtendedPlayer properties for entityPlayer
     * This method is for convenience only; it will make your code look nicer
     */
    public static final ExtendedPlayer get(EntityPlayer player) {
       return (ExtendedPlayer) player.getExtendedProperties(EXTENDED_ENTITY_TAG); // TODO: Добавить Exception, если null
    }

    public boolean isServerSide() {
        return this.entityPlayer instanceof EntityPlayerMP;
    }

    @Override
    public void saveNBTData(NBTTagCompound properties) {
        properties.setInteger("exp", exp);
        properties.setInteger("rank", rank.toInt());
        properties.setInteger("tiredness", tiredness);
        properties.setInteger("tirednessLimit", tirednessLimit);

        this.statsInventory.writeToNBT(properties);
        this.skillsInventory.writeToNBT(properties);
        this.wearableInventory.writeToNBT(properties);
        this.otherTabsHost.writeToNBT(properties);
        this.otherTabsInventory.writeToNBT(properties);
    }

    // TODO: Почему-то когда открывается GUI - Отображается категорий скиллов ловкости
    @Override
    public void loadNBTData(NBTTagCompound properties) {
        exp = properties.getInteger("exp");
        rank = Rank.fromInt(properties.getInteger("rank"));
        tiredness = properties.getInteger("tiredness");
        tirednessLimit = properties.getInteger("tirednessLimit");

        /* Нет нужды очищать инвентари перед применением сохранения, т.к.
         * readFromNBT() перезаписывает ВСЕ слоты инвентаря */
        this.statsInventory.readFromNBT(properties);
        this.skillsInventory.readFromNBT(properties);
        this.wearableInventory.readFromNBT(properties);
        this.otherTabsHost.readFromNBT(properties);
        this.otherTabsInventory.readFromNBT(properties);

        /* Т.к. ванильный инвентарь переписывать нежелательно, начальная инициализация модификатором от брони
         * реализована здесь */
        NBTTagList playerInventory = properties.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < playerInventory.tagCount(); i++) {
            NBTTagCompound itemNBT = playerInventory.getCompoundTagAt(i);
            int slotID = itemNBT.getInteger("Slot");
            if (slotID >= 100 && slotID <= 103) {
                modifierManager.addModifiersFrom( ItemStack.loadItemStackFromNBT(itemNBT) );
            }

        }

        // Выгружаем модификаторы перков из NBT-сохранения
        TabInventory.Tab a = otherTabsInventory.items.get(OtherItems.perksTabItem.getUnlocalizedName());
        for (ItemStack stack : a.stacks) {
            if (stack != null) {
                PerkItem perkItem = (PerkItem) stack.getItem();
                modifierManager.addModifiers(perkItem.getModifiers());
            }
        }

        updateParams();
    }

    /**
     * Used to initialize the extended properties with the entity that this is attached to, as well
     * as the world object.
     * Called automatically if you register with the EntityConstructing event.
     * May be called multiple times if the extended properties is moved over to a new entity.
     *  Such as when a player switches dimension {Minecraft re-creates the player entity}
     * @param entity  The entity that this extended properties is attached to
     * @param world  The world in which the entity exists
     */
    @Override
    public void init(Entity entity, World world) {
        /* Крайне интересный хак. Дело в том, что init() используется для инициализации самого ExtendedPlayer'а,
         * а не Compound'а, который передается в loadNBTData(). TODO: Я все еще не разобрался как связана сущность, создаваемая тут и compound в loadNBTData
         * Я проивожу "инициализацию нового игрока" тут, т.к. если игрок зашел в игру первый раз - loadNBTData никогда не
         * вызовется при логине. Подозреваю это из-за того, что на сервере нет NBT записи об этом игроке.
         * Зато когда она заходит во второй раз - loadNBTData() точно вызовется, но т.к. перед ним вызовется и init(), то
         * в loadNBTData() нужно предварительно очистить инициализацию нового игрока, которая выполнилась тут.
         */
        ExtendedPlayer.get((EntityPlayer) entity).statsInventory.initItems();
        ExtendedPlayer.get((EntityPlayer) entity).skillsInventory.initItems();

        ExtendedPlayer.get((EntityPlayer) entity).rank = Rank.NOVICE;

        // Инициализируем основные параметры
        updateParams();
    }

    /**
     * Инициализирует {@link #mainContainer}. Следует вызвать, когда ExtendedPlayer и все его инвентари будут
     * инициализированы и готовы к работе.
     */
    public void initContainer() {
        mainContainer = new MainContainer(this);
        if (isServerSide()) mainContainer.addCraftingToCrafters((EntityPlayerMP) getEntityPlayer());
    }

    /**
     * @param param Параметр игрока
     * @return Значение параметра игрока с учетом модификаторов
     */
    public int getParamWithModifiers(ParamKeys param) {
        switch (param) {
            case CHARISMA: return charisma+modifierManager.getTotalValue(param);
            case PERSISTENCE: return persistence+modifierManager.getTotalValue(param);
            case PROTECTION: return protection+modifierManager.getTotalValue(param);
            case STEP: return step+modifierManager.getTotalValue(param);
            case TIREDNESS: return tiredness+modifierManager.getTotalValue(param);
            case TIREDNESS_LIMIT: return tirednessLimit+modifierManager.getTotalValue(param);
        }

        throw new IllegalStateException("Impossible state. Contact me about it");
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    /**
     * Перерасчитывает параметры игрока (такие, как например, {@link #protection})
     */
    public void updateParams() {
        // Расчитываем параметр "Защита"
        ItemStack itemStack = ru.rarescrap.tabinventory.utils.Utils.findIn(
                skillsInventory,
                Utils.getRegistryName(SkillItems.fightingSkillItem),
                StatItems.agilityStatItem.getUnlocalizedName()); // TODO: UnlocalizedName используется в качестве ключа вкладки!

        if (itemStack != null) {
            if (itemStack.getItem().getDamage(itemStack) == 0) {
                this.protection = 2;
            } else {
                this.protection = 2 + StatItem.getRoll(itemStack).dice / 2;
            }
        }

        // Рассчитываем параметр "Стойкость"
        itemStack = Utils.findIn(statsInventory, Utils.getRegistryName(StatItems.enduranceStatItem));
        if (itemStack != null)
            this.persistence = 2 + StatItem.getRoll(itemStack).dice / 2;
    }

    /**
     * Синхронихронизиует серверного и клиентского ExtendedPlayer'а.
     */
    public void sync() {
        if(!entityPlayer.worldObj.isRemote) {
            CommonProxy.INSTANCE.sendTo(new PacketSyncPlayer(this), (EntityPlayerMP)entityPlayer);
        }
    }
}
