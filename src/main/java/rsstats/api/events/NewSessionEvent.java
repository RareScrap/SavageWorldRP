package rsstats.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.common.MinecraftForge;
import rsstats.data.ExtendedPlayer;

/**
 * NewSessionEvent выбрасывается когда началась новая игровая сессия.<br>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.
 */
public class NewSessionEvent extends ExtendedPlayerEvent { // TODO: Хз насчет реализации. Просто набросок
    public NewSessionEvent(ExtendedPlayer extendedPlayer) {
        super(extendedPlayer);
    }


}
