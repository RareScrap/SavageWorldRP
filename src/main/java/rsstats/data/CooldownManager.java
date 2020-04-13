package rsstats.data;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import rsstats.api.items.perk.PerkItem;
import rsstats.common.CommonProxy;
import rsstats.common.network.PacketCooldown;
import rsstats.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO: Делатьли абстрактным и продумывать возможность кастомного кд манагера?
// TODO: Как оно должно взаимодейтвовать с Rulebook'ом?
// TODO: Каким должно быть поведение при пропуске тиков из-за перегруза сервера?
// TODO: Что должно делаться с кд если игрок отключается от сервера?
// TODO: Что должно делаться с кд игроков если отключается сам сервер?
public class CooldownManager {
    private ExtendedPlayer player;
    private HashMap<PerkItem, CooldownData> cooldowns = new HashMap<PerkItem, CooldownData>();

    public CooldownManager(ExtendedPlayer player) {
        this.player = player;
    }

    public int getCooldown(PerkItem perkItem) {
        return cooldowns.get(perkItem).value; // TODO: Что будет если null?
    }

    public void setCooldown(PerkItem perkItem) {
        CooldownData cooldownData = new CooldownData(/*perkItem, */perkItem.getCooldown(player));
        if (cooldownData.value > 0) {
            cooldowns.put(perkItem, cooldownData);
            sync(player, perkItem, cooldownData);
        }
        // TODO: event?
    }

    public void setCooldown(PerkItem perkItem, int ticks) {
        if (ticks > 0) {
            CooldownData cooldownData = new CooldownData(ticks);
            cooldowns.put(perkItem, cooldownData);
            sync(player, perkItem, cooldownData);
        } // TODO: else log
    }

    public void tick() {
        for (Map.Entry<PerkItem, CooldownData> entry : cooldowns.entrySet()) {
            CooldownData cooldownData = entry.getValue();
            System.out.println("cooldown " + Utils.getRegistryName(entry.getKey()) + " = " + cooldownData.value);
            if (cooldownData.tick()) cooldowns.remove(entry.getKey()); // TODO: event?
        }
    }

    public void removeCooldown(PerkItem perkItem) {
        cooldowns.remove(perkItem);
    }

    public boolean isCooldown(PerkItem perkItem) {
        return cooldowns.containsKey(perkItem);
    }

    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound nbtCooldowns = new NBTTagCompound();

        for (Map.Entry<PerkItem, CooldownData> entry : cooldowns.entrySet()) {
            String id = GameRegistry.findUniqueIdentifierFor(entry.getKey()).toString();
            int cooldownValue = entry.getValue().value;
            nbtCooldowns.setInteger(id, cooldownValue);
        }

        compound.setTag("cooldowns", nbtCooldowns);
    }

    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound nbtCooldowns = compound.getCompoundTag("cooldowns");
        for (String id : (Set<String>) nbtCooldowns.func_150296_c()) {
            GameRegistry.UniqueIdentifier identifier = new GameRegistry.UniqueIdentifier(id);
            PerkItem perkItem = (PerkItem) GameRegistry.findItem(identifier.modId, identifier.name);
            cooldowns.put(perkItem, new CooldownData(/*perkItem, */nbtCooldowns.getInteger(id)));
        }
    }

    // TODO: А как быть с задержкой сети?
    private void sync(ExtendedPlayer player, PerkItem perkItem, CooldownData cooldownData) {
        NBTTagCompound cd = new NBTTagCompound();
        GameRegistry.UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(perkItem);
        cd.setInteger(identifier.toString(), cooldownData.value);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("cooldowns", cd);

        CommonProxy.INSTANCE.sendTo(new PacketCooldown(compound), (EntityPlayerMP) player.getEntityPlayer());
    }

    public void sync(ExtendedPlayer player) {
        NBTTagCompound cd = new NBTTagCompound();
        saveNBTData(cd);
        CommonProxy.INSTANCE.sendTo(new PacketCooldown(cd), (EntityPlayerMP) player.getEntityPlayer());
    }

    // TODO: Нормально ли оно будет вести себя в хешмапе, ведь я не переопределил equals и hashsum?
    private static class CooldownData {
//        private PerkItem perkItem;
        private int value;

//        public Cooldown(NBTTagCompound compound) {
//            GameRegistry.UniqueIdentifier identifier = new GameRegistry.UniqueIdentifier(compound.getString("perkItem"));
//            perkItem = (PerkItem) GameRegistry.findItem(identifier.modId, identifier.name);
//            value = compound.getInteger("value");
//            // TODO: Проверки на валидные данные
//        }

        public CooldownData(/*PerkItem perkItem, */int value) {
//            this.perkItem = perkItem;
            this.value = Math.max(value, 0);
        }

        boolean tick() {
            return --value == 0; // TODO: пре или пост декремент?
        }

//        public void saveNBTData(NBTTagCompound compound) {
//            GameRegistry.UniqueIdentifier identifier = GameRegistry.findUniqueIdentifierFor(perkItem);
//            compound.setString("perkItem", identifier.toString());
//            compound.setInteger("value", value);
//        }
    }
}
