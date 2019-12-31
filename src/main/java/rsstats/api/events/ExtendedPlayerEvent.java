package rsstats.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import rsstats.data.ExtendedPlayer;

/**
 * ExtendedPlayerEvent выбрасывается когда произошло событие связанное с {@link rsstats.data.ExtendedPlayer}'ом.<br>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 **/
public class ExtendedPlayerEvent extends PlayerEvent {
    public final ExtendedPlayer extendedPlayer;

    public ExtendedPlayerEvent(EntityPlayer player) {
        super(player);
        extendedPlayer = ExtendedPlayer.get(player); // TODO: А если null?
    }

    public ExtendedPlayerEvent(ExtendedPlayer extendedPlayer) { // TODO: А если null?
        super(extendedPlayer.getEntityPlayer());
        this.extendedPlayer = extendedPlayer;
    }
}
