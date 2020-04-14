package rsstats.data;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
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

import static rsstats.utils.Utils.millisToTicks;

// TODO: Делатьли абстрактным и продумывать возможность кастомного кд манагера?
// TODO: Как оно должно взаимодейтвовать с Rulebook'ом?
// TODO: Каким должно быть поведение при пропуске тиков из-за перегруза сервера?
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
        CooldownData cooldownData = cooldowns.get(perkItem); // TODO: Почему работало без этого?
        return cooldownData == null ? 0L : cooldownData.getTicksLeft(getTotalWorldTime()); // TODO: Что будет если null?
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
        } // TODO: else log
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
            NBTTagCompound nbtCooldownData = new NBTTagCompound();
//            nbtCooldown.setString("perkItem", id); // TODO: Не лучше ли использовать intID итема? Или это может привести к непредсказуемым последствиям при запуске мира в новейшей версии игры?
            nbtCooldownData.setLong("startTimestamp", entry.getValue().startTimestamp);
            nbtCooldownData.setLong("endTimestamp", entry.getValue().endTimestamp);
            nbtCooldownData.setLong("skippedTicks", entry.getValue().skippedTicks);
            String id = GameRegistry.findUniqueIdentifierFor(entry.getKey()).toString();
            nbtCooldowns.setTag(id, nbtCooldownData);
        }

        compound.setTag("cooldowns", nbtCooldowns);
    }

    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound nbtCooldowns = compound.getCompoundTag("cooldowns");
        for (String id : (Set<String>) nbtCooldowns.func_150296_c()) {
            GameRegistry.UniqueIdentifier identifier = new GameRegistry.UniqueIdentifier(id);
            PerkItem perkItem = (PerkItem) GameRegistry.findItem(identifier.modId, identifier.name);
            NBTTagCompound nbtCooldownData = nbtCooldowns.getCompoundTag(id);

            CooldownData cooldownData = new CooldownData(nbtCooldownData);
            if (!player.getEntityPlayer().worldObj.isRemote && !RSStats.proxy.ignoreDowntimeInCooldown) {
                long skippedTicks = (getTotalWorldTime() + millisToTicks(player.offlineTime)) - cooldownData.startTimestamp;
                if (cooldownData.startTimestamp + skippedTicks > cooldownData.endTimestamp) continue;
                cooldownData.skippedTicks = skippedTicks;
            }

            cooldowns.put(perkItem, cooldownData); // TODO: Может вообще пропускать просрочившиеся кулдауны?
        }
    }

    // TODO: Почему иногда улетает пустой компаунд?
    // TODO: А как быть с задержкой сети?
    private void sync(ExtendedPlayer player, PerkItem perkItem, CooldownData cooldownData) {
        NBTTagCompound nbtCooldowns = new NBTTagCompound();
        NBTTagCompound nbtCooldownData = new NBTTagCompound();
        NBTTagCompound compound = new NBTTagCompound();

        String identifier = GameRegistry.findUniqueIdentifierFor(perkItem).toString();
        cooldownData.saveNBTData(nbtCooldownData);
        nbtCooldowns.setTag(identifier, nbtCooldownData);
        compound.setTag("cooldowns", nbtCooldowns);

        CommonProxy.INSTANCE.sendTo(new PacketCooldown(compound), (EntityPlayerMP) player.getEntityPlayer());
    }

    public void sync(ExtendedPlayer player) {
        NBTTagCompound compound = new NBTTagCompound();
        saveNBTData(compound);
        CommonProxy.INSTANCE.sendTo(new PacketCooldown(compound), (EntityPlayerMP) player.getEntityPlayer());
    }

    private long getTotalWorldTime() {
        return player.getEntityPlayer().worldObj.getTotalWorldTime();
    }

    // TODO: Нормально ли оно будет вести себя в хешмапе, ведь я не переопределил equals и hashsum?
    private static class CooldownData {
        private final long startTimestamp;
        private final long endTimestamp;

        private long skippedTicks = 0L;

        public CooldownData(NBTTagCompound compound) {
            startTimestamp = compound.getLong("startTimestamp");
            endTimestamp = compound.getLong("endTimestamp");
            skippedTicks = compound.getLong("skippedTicks");
            // TODO: Проверки на валидные данные
        }

        // TODO: Может лучше передавать World?
        public CooldownData(long startTimestamp, int cooldown) {
            this.startTimestamp = startTimestamp;
            endTimestamp = startTimestamp + Math.max(cooldown, 0);
            // TODO: Лог при cooldown <= 0?
        }

        boolean tick(long totalWorldTime) {
            // TODO: что делать если cant keep up
            // TODO: что делать totalWorldTime перескочет за диапазон?
            return totalWorldTime+skippedTicks >= endTimestamp; // TODO: граничится только ==?
        }

        public void saveNBTData(NBTTagCompound compound) {
            compound.setLong("startTimestamp", startTimestamp);
            compound.setLong("endTimestamp", endTimestamp); // TODO: вычислять а не сохранять
            compound.setLong("skippedTicks", skippedTicks); // TODO: вычислять а не сохранять
        }

        public long getTicksLeft(long totalWorldTime) {
            return endTimestamp - totalWorldTime - skippedTicks;
        }
    }
}
