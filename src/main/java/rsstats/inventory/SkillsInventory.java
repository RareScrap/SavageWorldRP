package rsstats.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import rsstats.items.SkillItem;
import rsstats.items.SkillItems;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

import java.util.ArrayList;
import java.util.Map;

public class SkillsInventory extends TabInventory {
    /**
     * Конструктор, создающий инвентарь с указанными параметрами. После взова конструктора, вам
     * следует добавить в инвентарь вкладки вызовом метода {@link #addTab(String)}.
     *
     * @param inventoryName        Имя инвентарая, которое будет использовано при сохранении его содержимого в NBT
     * @param tabSlotsCount        Вместимость каждой вкладки
     * @param inventoryOwnerEntity Сущность, к которой привязан инвентарь
     * @param host                 Инвентарь, хранящий вкладки, которые и занимаются переключением контента
     */
    public SkillsInventory(String inventoryName, int tabSlotsCount, Entity inventoryOwnerEntity, TabHostInventory host) {
        super(inventoryName, tabSlotsCount, inventoryOwnerEntity, host);
    }

    /**
     * Проверяет, можно ли поместить предмет в данный слот инвентаря
     * <p>
     * This method doesn't seem to do what it claims to do, as
     * items can still be left-clicked and placed in the inventory
     * even when this returns false
     *
     * @param slotIndex TODO
     * @param itemStack Предмет, который хочет поместиться в инвентарь
     * @return Итог проверки: возвращает true, если предмет можно поместить в инвентарь.
     * Иначе - false.
     */
    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        return itemStack.getItem() instanceof SkillItem;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    // TODO: В утилиты
    public static boolean addToTab(Tab tab, ItemStack itemStack) {
        for (int i = 0; i < tab.stacks.length; i++) {
            ItemStack stack = tab.stacks[i];
            if (stack == null) {
                tab.setSlotContent(i, itemStack);
                return true;
            }
        }

        return false;
    }

    /**
     * Инициализирует начальные скиллы
     */
    public void initItems() {
        for (Map.Entry<String, SkillItem> entry : SkillItems.getAll().entrySet()) {
            String parentStatName = entry.getValue().parentStat.getUnlocalizedName();

            if (!hasTab(parentStatName)) {
                addTab(parentStatName);
            }

            addToTab(getTab(parentStatName), new ItemStack(entry.getValue()));
        }

        markDirty();
    }

    // TODO: Контейнер должен вызывать эти методы, как в ванильных блоках. Сейчас MainContainer их просто игнорирует. Это касается всех инвентарей
    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    // TODO: В утилиты
    /**
     * Получаем первый попавшийся стак из по указанному UnlocalizedName
     * @param unlocalizedSkillName UnlocalizedName нужного стака
     * @return Первый попавшийся полходящий стак. Если ничего не найдено - null.
     */
    public ItemStack getSkill(String unlocalizedSkillName) {
        for (Map.Entry<String, Tab> entry : items.entrySet()) {
            Tab tab = entry.getValue();

            for (ItemStack stack : tab.stacks) {
                if (stack != null && stack.getUnlocalizedName().equals(unlocalizedSkillName)) {
                    return stack;
                }
            }
        }

        return null;
    }

    /**
     * Очищает инвентарь, удаляя все вкладки
     */
    public void totalClear() {
        items.clear();
        setCurrentTab("");
    }

    public ArrayList<ItemStack> getSkills() {
        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        for (Map.Entry<String, Tab> entry : items.entrySet()) {
            Tab tab = entry.getValue();

            for (ItemStack stack : tab.stacks) {
                if (stack != null) returnList.add(stack);
            }
        }

        return returnList;
    }
}
