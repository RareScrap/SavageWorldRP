package rsstats.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Utils {
    /**
     * Проверяет базовый инвентарь игрока на наличи какого-либо предмета. Если нашел - возвращает стак этого предмета.
     * @param entityPlayer Целевой игрок
     * @param itemUnlocalizedName Имя предмета
     * @return Первый попавшийся стак, соответсвующий запросу
     */
    public static ItemStack isPlayerHave(EntityPlayer entityPlayer, String itemUnlocalizedName) {
        for (ItemStack stack : entityPlayer.inventory.mainInventory) {
            if (stack != null && stack.getItem().getUnlocalizedName().equals(itemUnlocalizedName)) {
                return stack;
            }
        }
        return null;
    }
}
