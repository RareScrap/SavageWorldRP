package rsstats.api.roll;

import net.minecraft.item.ItemStack;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItem;

import java.util.List;

import static rsstats.items.StatItem.getRoll;


/**
 * Бросок дайсов с информацией о том, кто делал бросок и для какого навыка
 * @author RareScrap
 */
//@SideOnly(Side.SERVER) // TODO: Нужно юзать роллы только на сервере, но клиенту тоже зачем-то они нужны (крашится без них, хотя нигде на клиенте не юзается). Исправить.
public class PlayerRoll extends Roll {
    public final ExtendedPlayer player;
    public ItemStack rollStack;

    public PlayerRoll(ExtendedPlayer player, ItemStack rollStack) {
        super(getRoll(rollStack));

        // Добавляем релефантные модификаторы, которыми обладает игрок
        List<RollModifier> modifiers = player.modifierManager.getModifiers((StatItem) rollStack.getItem()); // TODO: Как-то сцыкотно.
        if (modifiers != null)
            this.modifiers.addAll(modifiers);

        this.player = player;
        this.rollStack = rollStack;
    }
}
