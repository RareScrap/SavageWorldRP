package rsstats.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.world.World;
import rsstats.api.AbstractRulebook;

public class RulebookChangedEvent extends Event {
    public final AbstractRulebook deactivatedRulebook;
    public final AbstractRulebook currentRulebook;
    public final World world;

    public RulebookChangedEvent(AbstractRulebook deactivatedRulebook, AbstractRulebook currentRulebook, World world) {
        this.deactivatedRulebook = deactivatedRulebook;
        this.currentRulebook = currentRulebook;
        this.world = world;
    }
    
    public static class Pre extends RulebookChangedEvent {
        public Pre(AbstractRulebook deactivatedRulebook, AbstractRulebook currentRulebook, World world) {
            super(deactivatedRulebook, currentRulebook, world);
        }
    }
    
    public static class Post extends RulebookChangedEvent {
        public Post(AbstractRulebook deactivatedRulebook, AbstractRulebook currentRulebook, World world) {
            super(deactivatedRulebook, currentRulebook, world);
        }
    }
}
