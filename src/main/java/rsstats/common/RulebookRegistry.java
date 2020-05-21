package rsstats.common;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import rsstats.api.AbstractRulebook;
import rsstats.api.events.RulebookChangedEvent;
import rsstats.common.command.ClearRulebook;
import rsstats.common.command.GetActiveRulebook;
import rsstats.common.command.GetRulebooks;
import rsstats.common.command.SetRulebook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rsstats.common.RSStats.LOGGER;

public class RulebookRegistry {
    private static final EventHandler EVENT_HANDLER = new EventHandler();
    private static AbstractRulebook activeRulebook;
    private static HashMap<String, AbstractRulebook> rulebooks = new HashMap<String, AbstractRulebook>();

    @Deprecated
    public static boolean activateRulebook(String rulebookName, World world) {
        AbstractRulebook newRulebook = rulebooks.get(rulebookName);
        if (world.isRemote || newRulebook == null) return false;

        processRulebookChange(newRulebook, world);
        return true;
    }

    @SideOnly(Side.CLIENT)
    public static void applyToClient(AbstractRulebook rulebook) {
        processRulebookChange(rulebook, Minecraft.getMinecraft().theWorld);
    }

    private static void processRulebookChange(AbstractRulebook newRulebook, World world) {
        // Флаг, предотвращающий повторную (а следовательно и зацикленную) синхронизацию отключенного рулбука
        boolean flag = activeRulebook == null && newRulebook == null;
        MinecraftForge.EVENT_BUS.post(new RulebookChangedEvent.Pre(activeRulebook, newRulebook, world));
        activeRulebook = newRulebook; // TODO: Проверка на повторное применение активного провайдера
        if (!flag && !world.isRemote && shouldSyncRulebook()) syncWithAllPlayers();
        MinecraftForge.EVENT_BUS.post(new RulebookChangedEvent.Post(activeRulebook, newRulebook, world));
    }

    static boolean shouldSyncRulebook() {
        return MinecraftServer.getServer().isDedicatedServer() || ((IntegratedServer) MinecraftServer.getServer()).getPublic();
    }

    private static void syncWithAllPlayers() {
        if (activeRulebook == null) { // Уведомляем клиенты, если сервер отключает рулбук
            CommonProxy.INSTANCE.sendToAll(new ClearRulebook.Message());
            return;
        }

        for (WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            for (EntityPlayerMP player : (List<EntityPlayerMP>) worldServer.playerEntities) {
                activeRulebook.sync(player);
            }
        }
    }

    public static void registerRulebook(String rulebookName, AbstractRulebook rulebook) {
        rulebooks.put(rulebookName, rulebook); // TODO: Тут нужен эвент? Кому он может пригодиться?
        // TODO: евент при удалении системы веса
    }

    public static void clearRulebook(World world) {
        processRulebookChange(null, world); // TODO: ворлд нужен просто для евенов. Изменить бы это когда буду делать систему веса для каждого мира.
    }

    public static String[] getRulebooksNames() {
        return rulebooks.keySet().toArray(new String[0]);
    }

    public static String getActiveRulebookName() {
        for (Map.Entry<String, AbstractRulebook> entry : rulebooks.entrySet()) {
            if (entry.getValue() == activeRulebook) return entry.getKey();
        }
        throw new IllegalStateException("Active rulebook not set. Use getRulebook to check it.");
    }

    public static AbstractRulebook getActiveRulebook() {
        return activeRulebook;
    }

    public static AbstractRulebook getRulebook(String rulebookName) {
        return rulebooks.get(rulebookName);
    }

    public static boolean isRulebookActive(AbstractRulebook rulebook) {
        return activeRulebook == getActiveRulebook(); // TODO: Будет ли такое сравнение норм? Или сравнивать на принадлежность классу? Хз пока.
    }

    static void registerEventHandler() {
        MinecraftForge.EVENT_BUS.register(EVENT_HANDLER);
    }

    static int registerMessages(SimpleNetworkWrapper chanel, int discriminator) {
        chanel.registerMessage(ClearRulebook.MessageHandler.class, ClearRulebook.Message.class, discriminator++, Side.CLIENT);
        return discriminator;
    }

    static void registerServerCommands(FMLServerStartingEvent event) {
        event.registerServerCommand(new ClearRulebook());
        event.registerServerCommand(new GetActiveRulebook());
        event.registerServerCommand(new GetRulebooks());
        event.registerServerCommand(new SetRulebook());
    }

    public static class EventHandler {
        // Синхронизирует рулбук на сервере с клиентом при подключении игрока
        @SubscribeEvent(priority = EventPriority.LOW) // TODO: ХЗ качем приоритет. Но жопой чую что он должен работать после других эвентов
        public void onClientConnect(PlayerEvent.PlayerLoggedInEvent event) {
            if (!event.player.worldObj.isRemote && RulebookRegistry.getActiveRulebook() != null && RulebookRegistry.shouldSyncRulebook())
                RulebookRegistry.getActiveRulebook().sync((EntityPlayerMP) event.player);
        }

        // Восстанавливаем предыдущий рулбук, если таковой имеется
        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) { //TODO: FMLServerStartedEvent?
            if (!event.world.isRemote) RulebookWorldData.get(event.world).restoreLastRulebook(event.world); // TODO: Срабатывает для каждого димешна, что намекает на использования своей системы веса в каждой димешне. Текущее поведение хоть и работает норм, но не является верным. Возможно что использовать WorldSavedData было неудачным решением, т.к. сам этот механизм сохранет инфу в каждом из миров, коих много
        }

        // Сохраняем текущую систему веса, чтобы при следующем запуске сервера восстановить ее
        @SubscribeEvent
        public void onWorldSave(WorldEvent.Save event) { //TODO: FMLServerStoppedEvent?
            if (!event.world.isRemote) RulebookWorldData.get(event.world).markDirty();
        }
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    public static class RulebookWorldData extends WorldSavedData { // reflection needed modifier
        private String lastRulebook;

        public RulebookWorldData(String p_i2141_1_) { // reflection needed modifier
            super(p_i2141_1_);
        }

        static RulebookWorldData get(World world) {
            MapStorage storage = world.mapStorage;
            RulebookWorldData worldData = (RulebookWorldData) storage.loadData(RulebookWorldData.class, RSStats.MODID+"rulebook");

            if (worldData == null) {
                worldData = new RulebookWorldData(RSStats.MODID);
                storage.setData(RSStats.MODID, worldData);
            }

            return (RulebookWorldData) storage.loadData(RulebookWorldData.class, RSStats.MODID);
        }

        void restoreLastRulebook(World world) {
            if (lastRulebook != null && !RulebookRegistry.activateRulebook(lastRulebook, world)) {
                String logMsg = StatCollector.translateToLocalFormatted("log.rulebook.restore.warning",
                        lastRulebook, Arrays.asList(RulebookRegistry.getRulebooksNames()));
                LOGGER.warn(logMsg);
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound compound) {
            lastRulebook = compound.getString("last_rulebook");
        }

        @Override
        public void writeToNBT(NBTTagCompound compound) {
            if (RulebookRegistry.getActiveRulebook() != null)
                compound.setString("last_rulebook", RulebookRegistry.getActiveRulebookName());
        }
    }
}
