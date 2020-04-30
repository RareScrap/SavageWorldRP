package rsstats.data;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;
import rsstats.api.items.perk.PerkItem;
import rsstats.common.CommonProxy;
import rsstats.common.RSStats;
import rsstats.common.network.PacketCooldown;
import rsstats.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static rsstats.common.RSStats.LOGGER;
import static rsstats.utils.Utils.millisToTicks;

// TODO: Делатьли абстрактным и продумывать возможность кастомного кд манагера?
// TODO: Как оно должно взаимодейтвовать с Rulebook'ом?
// TODO: Каким должно быть поведение при пропуске тиков из-за перегруза сервера?
// TODO: Консольные команды для работы с кулдаунами
/**
 * Менеджер времени восстановления перков (кулдаунов).
 * Отвечает за предоставление информации об оставшемся кулдауне в тиках,
 * синхронизации кулдаунов с клиентом, установку и своевременное удаление кулдаунов.
 * <br/>
 * Синхронизация построена на основе {@link WorldInfo#getWorldTotalTime()}, который
 * синхронизируется с клиентами самой игрой. Такой подход уменьшает вероятность
 * рассинхронизации клиентского и серверного CooldownManager'а.
 * <br/>
 * При отключении игрока от <strong>продолжающего работу</strong> сервера, время, проведенное
 * игроком в офлафне будет вычтено из кулдаунов. Таким образом
 * кулдаун игрока будет расчитываться даже при выходе его из сервера.
 * <br/>
 * В случае если сервер был остановлен и снова запущен через некоторое время,
 * время, в течении которого был отключен сервер, может либо влиять на кулдаун, либо
 * игнорироваться. См. {@link CommonProxy#ignoreDowntimeInCooldown}
 */
public class CooldownManager {
    private ExtendedPlayer player;
    private HashMap<PerkItem, CooldownData> cooldowns = new HashMap<PerkItem, CooldownData>();

    public CooldownManager(ExtendedPlayer player) {
        this.player = player;
    }

    public long getCooldown(PerkItem perkItem) {
        CooldownData cooldownData = cooldowns.get(perkItem);
        return cooldownData == null ? 0L : cooldownData.getTicksLeft(getTotalWorldTime());
    }

    public void setCooldown(PerkItem perkItem) {
        setCooldown(perkItem, perkItem.getCooldown(player));
    }

    public void setCooldown(PerkItem perkItem, int ticks) {
        if (ticks > 0) {
            CooldownData cooldownData = new CooldownData(getTotalWorldTime(), ticks);
            cooldowns.put(perkItem, cooldownData);
            sync(player, perkItem, cooldownData);
            // TODO: event?
        } else LOGGER.info("Attempt to set a negative cooldown (%d ticks) for perk \"%s\"", ticks, getID(perkItem));
    }

    public void tick() {
        for (Map.Entry<PerkItem, CooldownData> entry : cooldowns.entrySet()) {
            CooldownData cooldownData = entry.getValue();
            System.out.println("cooldown " + Utils.getRegistryName(entry.getKey()) + " = " + cooldownData.getTicksLeft(getTotalWorldTime()));
            if (cooldownData.tick(getTotalWorldTime())) removeCooldown(entry.getKey());
        }
    }

    public void removeCooldown(PerkItem perkItem) {
        cooldowns.remove(perkItem);
        // TODO: sync
        // TODO: event?
    }

    public boolean isCooldown(PerkItem perkItem) {
        return cooldowns.containsKey(perkItem);
    }

    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound nbtCooldowns = new NBTTagCompound();
        for (Map.Entry<PerkItem, CooldownData> entry : cooldowns.entrySet()) {
            nbtCooldowns.setLong(getID(entry.getKey()), entry.getValue().endTimestamp);
        }

        compound.setTag("cooldowns", nbtCooldowns);
    }

    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound nbtCooldowns = compound.getCompoundTag("cooldowns");
        for (String id : (Set<String>) nbtCooldowns.func_150296_c()) {
            long endTimestamp = nbtCooldowns.getLong(id);
            CooldownData cooldownData = new CooldownData(endTimestamp);

            if (!player.getEntityPlayer().worldObj.isRemote && !RSStats.proxy.ignoreDowntimeInCooldown) {
                long downtimeTicks = millisToTicks(player.offlineTime);
                if (cooldownData.mergeDowntime(downtimeTicks)) continue; // TODO: event для завершившихся кулдаунов?
            }

            cooldowns.put(getPerkItem(id), cooldownData);
        }
    }

    // TODO: Почему иногда улетает пустой компаунд?
    /**
     * Отсылает на указанный клиент кулдаун для одного перка
     * @param player Игрок, на клиент которого нужно послать пакет
     */
    private void sync(ExtendedPlayer player, PerkItem perkItem, CooldownData cooldownData) {
        NBTTagCompound compound = new NBTTagCompound();
        appendToSyncNBT(compound, perkItem, cooldownData);
        CommonProxy.INSTANCE.sendTo(new PacketCooldown(compound), (EntityPlayerMP) player.getEntityPlayer());
    }

    /**
     * Отсылает на указанный клиент все имеющиеся кулдауны одним пакетом
     * @param player Игрок, на клиент которого нужно послать пакет
     */
    public void sync(ExtendedPlayer player) {
        if (cooldowns.isEmpty()) return;

        NBTTagCompound compound = new NBTTagCompound();
        for (Map.Entry<PerkItem, CooldownData> e : cooldowns.entrySet())
            appendToSyncNBT(compound, e.getKey(), e.getValue());
        CommonProxy.INSTANCE.sendTo(new PacketCooldown(compound), (EntityPlayerMP) player.getEntityPlayer());
    }

    /**
     * Присоединяет перк и его кулдаун к NBT, формируя специальный тег предназначенный для синхронизаци
     * с клиентами. В отличии от {@link #saveNBTData(NBTTagCompound)} полученный тег не может быть использован
     * для сохранения состояния менеджера, т.к. строится на изменяемых от версии к версии данных.
     * @param syncNBT Тег, к которому нужно присоединить данные
     * @param perkItem Перк, кулдаун которого нужно синхронизировать
     * @param cooldownData Кулдаун перка
     */
    public void appendToSyncNBT(NBTTagCompound syncNBT, PerkItem perkItem, CooldownData cooldownData) {
        syncNBT.setLong(String.valueOf(Item.getIdFromItem(perkItem)), cooldownData.endTimestamp);
    }

    @SideOnly(Side.CLIENT)
    public void parseSyncNBt(NBTTagCompound syncNBT) {
        for (String itemId : (Set<String>) syncNBT.func_150296_c()) {
            PerkItem perkItem = (PerkItem) Item.getItemById(Integer.parseInt(itemId));
            cooldowns.put(perkItem, new CooldownData(syncNBT.getLong(itemId)));
        }
    }

    private long getTotalWorldTime() {
        return player.getEntityPlayer().worldObj.getTotalWorldTime();
    }

    private static PerkItem getPerkItem(String itemIdentifier) {
        GameRegistry.UniqueIdentifier identifier = new GameRegistry.UniqueIdentifier(itemIdentifier);
        return  (PerkItem) GameRegistry.findItem(identifier.modId, identifier.name);
    }

    private static String getID(PerkItem perkItem) {
        return GameRegistry.findUniqueIdentifierFor(perkItem).toString();
    }

    /**
     * Хранит информацию об оставшихся тиках одного кулдауна.
     */
    private static class CooldownData {
        /* Благодаря вычилению на сервере времени, при которм кулдан
         * должен закончится, можно не брать во внимание задерку сети. */
        private long endTimestamp;

        public CooldownData(long startTimestamp, int cooldown) {
            endTimestamp = startTimestamp + Math.max(cooldown, 0);
        }

        public CooldownData(long endTimestamp) {
            this.endTimestamp = endTimestamp;
        }

        boolean tick(long totalWorldTime) {
            // TODO: что делать если cant keep up
            // TODO: что делать totalWorldTime перескочет за диапазон?
            return totalWorldTime >= endTimestamp; // TODO: граничится только ==?
        }

        public NBTTagCompound saveNBTData(String perkItemId) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong(perkItemId, endTimestamp);
            return compound;
        }

        public long getTicksLeft(long totalWorldTime) {
            return endTimestamp - totalWorldTime;
        }

        /**
         * Принимает в расчет кулдауна время, в течении которог сервер был выулючен.
         * @param downtimeTicks Время, которое сервер провел в выключенном состоянии (в тиках)
         * @return True, если кулдаун уже успел закончится. Иначе - false.
         */
        public boolean mergeDowntime(long downtimeTicks) {
            return (endTimestamp -= downtimeTicks) <= 0;
        }
    }
}
