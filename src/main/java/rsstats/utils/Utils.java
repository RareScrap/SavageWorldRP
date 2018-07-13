package rsstats.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Находит в инвентаре все предметы с указанным имененем и отнимает указанное количество
     * @param inventory Инвентарь, в котором происходится поиск
     * @param targetItemUnlocalizedName UnlocalizedName целевого предмета
     * @param amount Сколько предметов следует удалить из стаков, имеющих этот предмет
     * @return False, если в инвенторе нет указанного количества предметов (в этом случае никакого удаления не происзводится).
     *         True, если удаление выполнено успешно.
     */
    public static boolean removeItemStackFromInventory(IInventory inventory, String targetItemUnlocalizedName, int amount) {
        int count = 0; // Находим общее число предметов во всех стаках
        Map<ItemStack, Integer> relevantStacks = new HashMap<ItemStack, Integer>();
        for (int slotIndex = 0; slotIndex < inventory.getSizeInventory(); slotIndex++) {
            ItemStack itemStack = inventory.getStackInSlot(slotIndex);
            if (itemStack != null && itemStack.getUnlocalizedName().equals(targetItemUnlocalizedName)) {
                count += itemStack.stackSize;
                relevantStacks.put(itemStack, slotIndex);
            }
        }

        if (count < amount) { // Невозможно продолжить удаление, если удаляемые элементы превышают количство имеющихся
            return false;
        }

        for (ItemStack itemStack : relevantStacks.keySet()) {
            if (itemStack.stackSize > amount) { // Для удаление достаточно уменьшить размер уже имеющегося стака
                itemStack.stackSize -= amount;
                return true;
            } else if (itemStack.stackSize == amount) { // Для удаления достаточно удалить имеющийся стак
                inventory.setInventorySlotContents(relevantStacks.get(itemStack), null);
                return true;
            } else { // itemStack.stackSize < amount
                amount -= itemStack.stackSize; // Даже удалив весь стак, мы не удалим указанное количество элементов
                inventory.setInventorySlotContents(relevantStacks.get(itemStack), null);
            }
        }

        throw new RuntimeException("Unplanned case. This is probably our bug.");
    }
}
