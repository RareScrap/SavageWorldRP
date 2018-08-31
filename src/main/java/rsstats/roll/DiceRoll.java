package rsstats.roll;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItem;

/**
 * Бросок дайсов с информацией о том, кто делал бросок и для какого навыка
 * @author RareScrap
 */
@SideOnly(Side.SERVER)
public class DiceRoll extends BasicRoll { // TODO: rename to PlayerRoll
    public final EntityPlayerMP player;
    public ItemStack rollStack;

    public DiceRoll(EntityPlayerMP player, ItemStack rollStack) {
        super( // TODO: Как-то уродливо
                ((StatItem) rollStack.getItem()).getRollLevel(rollStack),
                ExtendedPlayer.get(player).getModifierMap().get(rollStack.getUnlocalizedName())
        );
        this.player = player;
        this.rollStack = rollStack;
    }
}
