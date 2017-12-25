package rsstats.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import rsstats.common.RSStats;
import rsstats.inventory.SkillsInventory;
import rsstats.inventory.StatsInventory;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;

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
    public final StatsInventory statsInventory = new StatsInventory();
    /** Инвентарь для скиллов */
    public final SkillsInventory skillsInventory = new SkillsInventory();
    
    /*
    Тут в виде полей можно хранить дополнительную информацию о Entity: мана,
    золото, хп, переносимый вес, уровень радиации, репутацию и т.д. Т.е. все то,
    что нельзя хранить в виде блоков
    */

    public ExtendedPlayer(EntityPlayer player) {
        this.entityPlayer = player;
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
    }

    @Override
    public void loadNBTData(NBTTagCompound properties) {
        exp = properties.getInteger("exp");
        lvl = properties.getInteger("lvl");
        tiredness = properties.getInteger("tiredness");
        tirednessLimit = properties.getInteger("tirednessLimit");

        this.statsInventory.readFromNBT(properties);
        this.skillsInventory.readFromNBT(properties);
    }

    @Override
    public void init(Entity entity, World world) {
        // Инициализируем основные параметры
        loadNBTData(entity.getEntityData());
        try {
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

    public void setLvl(int lvl) {
        this.lvl = lvl;
    }
}
