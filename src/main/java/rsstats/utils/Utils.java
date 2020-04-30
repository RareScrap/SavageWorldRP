package rsstats.utils;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    /**
     * Проверяет инвентарь на наличи какого-либо предмета. Если нашел - возвращает стак этого предмета.
     * @param inventory Целевой инвентарь
     * @param item Предмет, стак с которы нужно найти
     * @return Первый попавшийся стак, соответсвующий запросу. Если не нашел - null.
     */
    public static ItemStack findIn(IInventory inventory, Item item) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;
            if (stack.getItem() == item) return stack;
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

    // TODO: Различается ли представление строки кд в разных локалях?
    /**
     * Преобразует время, указанное в тиках с строковое представление в формате "HH mm ss".
     * Нувые элементы игнорируются
     * @param ticks Время в тиках игры (20 тиков = 1 сек)
     * @return Время в формате "HH mm ss"
     */
    public static String formatCooldownTime(long ticks) {
        int secs = (int) (ticks / 20.0D);
        int mins = (int) (secs / 60.0D);
        int hrs = (int) (mins / 60.0D);
//            int days = (int) (hours / 24.0D);
//            int years = (int) (days / 365.0D);

        //Calculate the seconds to display:
        int seconds = secs %60;
        secs -= seconds;
        //Calculate the minutes:
        long minutesCount = secs / 60;
        long minutes = minutesCount % 60;
        minutesCount -= minutes;
        //Calculate the hours:
        long hours = minutesCount / 60;
        String s = seconds + " s"; // TODO: Брать время из файлов локализации
        String m = minutes > 0 ? minutes+" m " : "";
        String h = hours > 0 ? hours+" h " : "";

        return h+m+s;
    }

    public static long millisToTicks(long millis) {
        return millis / 50; // 50мс = 1 тик
    }

    /**
     * Возвращает {@link EnumChatFormatting} соответствующий коду форматирования
     * @param code Код форматирования
     * @return Подходящий EnumChatFormatting. Если таковой не найдет - null.
     */
    public static EnumChatFormatting getChatFormattingFromCode(String code) {
        for (EnumChatFormatting value : EnumChatFormatting.values()) {
            if (value.getFormattingCode() == code.charAt(0)) return value;
        }
        return null;
    }
}
