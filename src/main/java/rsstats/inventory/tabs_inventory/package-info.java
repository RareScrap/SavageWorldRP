/**
 * Классы инвентарей, используемые для реализации системы вкладок.
 *
 * <h1>Описание</h1>
 *
 * {@link rsstats.inventory.tabs_inventory.TabInventory}: Инвентарь, хранящий массивы стаков в формате "ключ-значение".
 * Ключи определяются объектом TabHostInventory, к которому присоединен данный TabInventory, и соответствуют нелокализированым
 * названиям предметов в TabHostInventory. Так например, если в TabHostInventory имеет 2 стака с предметами "Алмазный
 * топор" и "Семена пшеницы", то ключи вкладок будут ""item.hatchetDiamond" и "item.seeds".
 *
 * {@link rsstats.inventory.tabs_inventory.TabHostInventory}: обычный инвентарь, с которым связвается экземпляр
 * TabInventory. Отвечает за выбор текущей вкладки, добавление/удаление вкладок.
 *
 */
package rsstats.inventory.tabs_inventory;