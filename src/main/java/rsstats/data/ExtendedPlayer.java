package rsstats.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketSyncPlayer;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.inventory.WearableInventory;
import rsstats.inventory.tabs_inventory.TabHostInventory;
import rsstats.inventory.tabs_inventory.TabInventory;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.utils.RollModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rares
 */
public class ExtendedPlayer implements IExtendedEntityProperties {
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
    private int lvl = 0;
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

    /** Хранилище модификаторов, преминимых с броскам данного игрока */
    private Map<String, ArrayList<RollModifier>> modifierMap = new HashMap<String, ArrayList<RollModifier>>();
    
    /*
    Тут в виде полей можно хранить дополнительную информацию о Entity: мана,
    золото, хп, переносимый вес, уровень радиации, репутацию и т.д. Т.е. все то,
    что нельзя хранить в виде блоков
    */

    private ExtendedPlayer(EntityPlayer player) {
        this.entityPlayer = player;
        statsInventory = new StatsInventory(player);
        skillsInventory = new SkillsInventory(player);
        wearableInventory = new WearableInventory(this);

        otherTabsInventory = new TabInventory("effects", 36, entityPlayer);
        otherTabsHost = new TabHostInventory("effects_host", 4, otherTabsInventory);
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
       return (ExtendedPlayer) player.getExtendedProperties(EXTENDED_ENTITY_TAG);
    }

    public boolean isServerSide() {
        return this.entityPlayer instanceof EntityPlayerMP;
    }

    @Override
    public void saveNBTData(NBTTagCompound properties) {
        properties.setInteger("exp", exp);
        properties.setInteger("lvl", lvl);
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
        this.skillsInventory.totalClear();

        exp = properties.getInteger("exp");
        lvl = properties.getInteger("lvl");
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
                extractModifiersFromItemStack(ItemStack.loadItemStackFromNBT(itemNBT));
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

        // Инициализируем основные параметры
        //loadNBTData(entity.getEntityData());
        try { // КОСТЫЛЬ
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

    public int getLvl() {
        return lvl;
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getTirednessLimit() {
        return tirednessLimit;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public Map<String, ArrayList<RollModifier>> getModifierMap() {
        return modifierMap;
    }

    public void addModifier(String key, RollModifier modifier) {
        if (modifierMap.get(key) == null) {
            modifierMap.put(key, new ArrayList<RollModifier>());
        }

        modifierMap.get(key).add(modifier);
    }

    public void removeModifier(String key, int modifierValue, String modifierDescr) {
        if (modifierMap.get(key) == null) {
            return;
        }

        for (int i = 0; i < modifierMap.get(key).size(); i++) {
            RollModifier modifier = modifierMap.get(key).get(i);
            if (modifier.getValue() == modifierValue && modifierDescr.equals(modifier.getDescription())) {
                modifierMap.get(key).remove(modifier);
                return;
            }
        }
    }

    public void setLvl(int lvl) {
        this.lvl = lvl;
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
            CommonProxy.INSTANCE.sendTo(new PacketSyncPlayer(statsInventory.getStats(), skillsInventory.getSkills(), lvl), (EntityPlayerMP)entityPlayer);
        }
    }

    /**
     * Вытаскивает модификаторы из стака и добавляет их пользователю
     * @param itemStack предмет с модификаторами
     */
    public void extractModifiersFromItemStack(ItemStack itemStack) {
        if (itemStack != null && itemStack.getTagCompound() != null) { // извлекаем и сохраняем модификаторы
            NBTTagList modifiersList = itemStack.getTagCompound().getTagList("modifiers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < modifiersList.tagCount(); i++) {
                NBTTagCompound modifierTag = modifiersList.getCompoundTagAt(i);
                int value = modifierTag.getInteger("value");
                String description = modifierTag.getString("description");
                String to = modifierTag.getString("to");
                RollModifier modifier = new RollModifier(value, description); // TODO: Замечен странный баг. При создании modifier с руским Description он создается нормально ...
                this.addModifier(to, modifier); // ... Но при входе в этот метод все русские буквы из поля modifier.description удаляются! Как? Без понятия.
            }
        }
    }

    /**
     * Вытаскивает модификаторы из стака и ищет их среди подификаторов пользователя.
     * Если модификатор найден - он удаляется из модификаторов игрока
     * @param itemStack предмет с модификаторами
     */
    public void removeModifiersFromItemStack(ItemStack itemStack) {
        if (itemStack != null && itemStack.getTagCompound() != null) {
            NBTTagList modifiersList = itemStack.getTagCompound().getTagList("modifiers", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < modifiersList.tagCount(); i++) {
                NBTTagCompound modifierTag = modifiersList.getCompoundTagAt(i);
                int value = modifierTag.getInteger("value");
                String description = modifierTag.getString("description");
                String to = modifierTag.getString("to");
                this.removeModifier(to, value, description);
            }
        }
    }
}
