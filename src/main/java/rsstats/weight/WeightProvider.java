package rsstats.weight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import rsstats.common.CommonProxy;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItem;
import rsstats.items.StatItems;
import ru.rarescrap.simpleweightsystem.ConfigurableWeightProvider;
import ru.rarescrap.weightapi.WeightRegistry;
import ru.rarescrap.weightapi.event.CalculateMaxWeightEvent;

import java.io.File;
import java.util.Map;

public class WeightProvider extends ConfigurableWeightProvider { // TODO: Возможно придется отказаться от наследования, как только я определюсь с эффектом перегруза
    public WeightProvider(Map<Item, Double> weightStorage, double defaultWeight) {
        super(weightStorage, defaultWeight);
    }

    public WeightProvider(File configFile) {
        super(configFile);
    }

    @Override
    public double getMaxWeight(IInventory inventory, Entity owner) {
        double maxWeight;
        if (owner instanceof EntityPlayer) {
            ExtendedPlayer player = ExtendedPlayer.get((EntityPlayer) owner);
            maxWeight = player.weightModifier * StatItem.getRoll(player.getStat(StatItems.strenghtStatItem)).dice;
        } else maxWeight = inventory.getSizeInventory() * 64;

        CalculateMaxWeightEvent event = new CalculateMaxWeightEvent(inventory, maxWeight, owner);
        MinecraftForge.EVENT_BUS.post(event);
        return event.maxWeight;
    }

    @Override
    public void sync(EntityPlayerMP player) {
        CommonProxy.INSTANCE.sendTo(new SyncMessage(this), player);
    }

    /**
     * Пакет, доставляющий таблицу весов на клиент
     */
    public static class SyncMessage extends ConfigurableWeightProvider.SyncMessage {

        public SyncMessage() {} // for reflection newInstance()

        public SyncMessage(WeightProvider serverWeightProvider) {
            super(serverWeightProvider);
        }

        @Override
        protected ConfigurableWeightProvider constructWeightProvider(Map<Item, Double> weightStorage, double defaultWeight) {
            return new WeightProvider(weightStorage, defaultWeight);
        }
    }

    public static class MessageHandler implements IMessageHandler<SyncMessage, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(SyncMessage message, MessageContext ctx) {
            WeightRegistry.applyToClient(message.getWeightProvider());
            return null;
        }
    }
}
