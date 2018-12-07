package rsstats.roll;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import java.util.List;

import static rsstats.data.ExtendedPlayer.getModifiersFor;
import static rsstats.utils.Utils.getBasicRollFrom;

/**
 * Бросок дайсов с информацией о том, кто делал бросок и для какого навыка
 * @author RareScrap
 */
//@SideOnly(Side.SERVER) // TODO: Нужно юзать роллы только на сервере, но клиенту тоже зачем-то они нужны (крашится без них, хотя нигде на клиенте не юзается). Исправить.
public class DiceRoll extends BasicRoll { // TODO: rename to PlayerRoll
    public final EntityPlayerMP player;
    public ItemStack rollStack;

    public DiceRoll(EntityPlayerMP player, ItemStack rollStack) {
        super(getBasicRollFrom(rollStack));

        // Добавляем релефантные модификаторы, которыми обладает игрок
        ExtendedPlayer extendedPlayer = ExtendedPlayer.get(player);
        List<RollModifier> modifiers = extendedPlayer.modifierManager.getModifiers((StatItem) rollStack.getItem()); // TODO: Как-то сцыкотно.
        if (modifiers != null)
            this.modifiers.addAll(modifiers);

        this.player = player;
        this.rollStack = rollStack;
    }
}
