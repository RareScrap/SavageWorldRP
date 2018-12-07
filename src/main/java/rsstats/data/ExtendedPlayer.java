package rsstats.data;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.items.perk.IModifierDependent;
import rsstats.roll.RollModifier;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

import java.util.List;

/**
 *
 * @author rares
 */
public class ExtendedPlayer implements IExtendedEntityProperties {

    public enum Rank {
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

        @SideOnly(Side.CLIENT)
        public String getTranslatedName() {
            return StatCollector.translateToLocal("rank." + this.name().toLowerCase());
        }
    }

    /**
     * Ключи статичных параметров игрока
     */
    public enum ParamKeys implements IModifierDependent { // TODO: Упростить get и set методы ExtendedPlayer'а, использая в качестве параметра константы из енума Rank
        STEP,
        PROTECTION,
        PERSISTENCE,
        CHARISMA
    }

    /** Каждый наследник {@link IExtendedEntityProperties} должен иметь индивидуальное имя */
    private static final String EXTENDED_ENTITY_TAG = RSStats.MODID;

    private final EntityPlayer entityPlayer;

    /** Основной параметр игрока - Шаг */
    private int step = 6;
    /** Основной параметр игрока - Защита */
    private int protection;
    /** Основной параметр игрока - Стойкость */
    private int persistence;
    /** Основной параметр игрока - Харизма */
    private int charisma = 0;

    private int exp = 0;
    private Rank rank;
    private int tiredness = 0;
    private int tirednessLimit = 25;
    
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

//    /** Хранилище модификаторов, преминимых с броскам данного игрока */
//    private Map<String, ArrayList<RollModifier>> modifierMap = new HashMap<String, ArrayList<RollModifier>>();
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
        this.statsInventory.totalClear();
        this.skillsInventory.totalClear(); // TODO: Может ли воникнуть ситуация, когда инвентари не пустые?

        exp = properties.getInteger("exp");
        rank = Rank.fromInt(properties.getInteger("rank"));
        tiredness = properties.getInteger("tiredness");
        tirednessLimit = properties.getInteger("tirednessLimit");

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
        //loadNBTData(entity.getEntityData());
        try { // TODO: КОСТЫЛЬ
            ItemStack itemStack = skillsInventory.getSkill("item.FightingSkillItem");
            if (itemStack.getItem().getDamage(itemStack) == 0) {
                this.protection = 2;
            } else {
                this.protection = 2 + ((SkillItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
            }
            itemStack = statsInventory.getStat("item.EnduranceStatItem");
            this.persistence = 2 + ((StatItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
        } catch (Exception e) {}
    }

    public int getProtection() {
        return protection;
    }

    public int getStep() {
        return step;
    }

    public int getPersistence() {
        return persistence;
    }

    public int getCharisma() {
        return charisma;
    }

    public int getExp() {
        return exp;
    }

    public Rank getRank() {
        return rank;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getTirednessLimit() {
        return tirednessLimit;
    }

    public void setProtection(int protection) {
        this.protection = protection;
    }

    public void setPersistence(int persistence) {
        this.persistence = persistence;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    /**
     * Перерасчитывает параметры игрока (такие, как например, {@link #protection})
     */
    public void updateParams() {
        ItemStack itemStack = skillsInventory.getSkill("item.FightingSkillItem");
        if (itemStack != null) {
            if (itemStack.getItem().getDamage(itemStack) == 0) {
                this.protection = 2;
            } else {
                this.protection = 2 + ((SkillItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
            }
        }

        itemStack = statsInventory.getStat("item.EnduranceStatItem");
        if (itemStack != null) {
            this.persistence = 2 + ((StatItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
        }
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
