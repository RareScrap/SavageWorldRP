package rsstats.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import rsstats.items.SkillItem;
import rsstats.items.SkillItems;
import rsstats.items.StatItem;
import rsstats.items.StatItems;
import rsstats.roll.BasicRoll;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    /**
     * Проверяет инвентарь на наличи какого-либо предмета. Если нашел - возвращает стак этого предмета.
     * @param inventory Целевой инвентарь
     * @param itemRegistryName Уникальное имя предмета
     * @return Первый попавшийся стак, соответсвующий запросу. Если не нашел - null.
     */
    public static ItemStack findIn(IInventory inventory, String itemRegistryName) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;

            String stackItemName = getRegistryName(stack.getItem());
            if (stackItemName.equals(itemRegistryName)) return stack;
        }

        return null;
    }

    /**
     * Метод-обертка, возвраюащее уникальное имя предмета, которое было дано ему при регистрации
     */
    public static String getRegistryName(Item item) {
        return GameRegistry.findUniqueIdentifierFor(item).name;
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

    /**
     * Пытается получить базовый ролл для итема в стаке по метадате итема
     * @param itemStack Стак с {@link StatItem}'ом. Метадата стака должна обозначать уровень навыка.
     * @throws ClassCastException Если itemStack не содержит {@link StatItem}.
     */
    public static BasicRoll getBasicRollFrom(ItemStack itemStack) {
        StatItem statItem = (StatItem) itemStack.getItem();

        if (statItem instanceof SkillItem) {
            return SkillItems.basicRolls.get(itemStack.getItemDamage());
        } else {
            return StatItems.basicRolls.get(itemStack.getItemDamage());
        }
    }
}
