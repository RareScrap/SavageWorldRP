package rsstats.api;

import net.minecraft.entity.player.EntityPlayerMP;
import rsstats.data.ExtendedPlayer;
import rsstats.utils.OperationResult;

public abstract class AbstractRulebook {
    private final ExtendedPlayer player;

    public AbstractRulebook(ExtendedPlayer player) {
        this.player = player;
    }

    public abstract OperationResult onExpChanged(int expPoints);

    public abstract void sync(EntityPlayerMP player);
}
