package rsstats.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
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
    private static final String INV_NAME = "StatsInventory";
    
    private final EntityPlayer entityPlayer;

    /** Основной параметр игрока - Шаг */
    private int step = 6;
    /** Основной параметр игрока - Защита */
    private int protection;
    /** Основной параметр игрока - Стойкость */
    private int persistence;
    /** Основной параметр игрока - Харизма */
    private int charisma = 0;
    
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
        player.registerExtendedProperties(ExtendedPlayer.INV_NAME, new ExtendedPlayer(player));
    }
    
    /**
     * Returns ExtendedPlayer properties for entityPlayer
     * This method is for convenience only; it will make your code look nicer
     */
    public static final ExtendedPlayer get(EntityPlayer player) {
       return (ExtendedPlayer) player.getExtendedProperties(INV_NAME);
    }

    public boolean isServerSide() {
        return this.entityPlayer instanceof EntityPlayerMP;
    }

    // LOAD, SAVE =============================================================

    @Override
    public void saveNBTData(NBTTagCompound properties) {
        this.statsInventory.writeToNBT(properties);
        this.skillsInventory.writeToNBT(properties);
    }

    @Override
    public void loadNBTData(NBTTagCompound properties) {
        this.statsInventory.readFromNBT(properties);
        this.skillsInventory.readFromNBT(properties);
    }

    @Override
    public void init(Entity entity, World world) {
        // Инициализируем основные параметры
        loadNBTData(entity.getEntityData());
        ItemStack itemStack = skillsInventory.getSkill("item.FightingSkillItem");
        if (itemStack.getItem().getDamage(itemStack) == 0) {
            this.protection = 2;
        } else {
            this.protection = 2 + ((SkillItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
        }
        itemStack = statsInventory.getStat("item.EnduranceStatItem");
        this.persistence = 2 + ((StatItem) itemStack.getItem()).getRollLevel(itemStack) / 2;
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
}
