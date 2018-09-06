package rsstats.roll;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItem;

/**
 * Бросок дайсов с информацией о том, кто делал бросок и для какого навыка
 * @author RareScrap
 */
//@SideOnly(Side.SERVER) // TODO: Нужно юзать роллы только на сервере, но клиенту тоже зачем-то они нужны (крашится без них, хотя нигде на клиенте не юзается). Исправить.
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
