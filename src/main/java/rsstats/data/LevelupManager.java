package rsstats.data;

import net.minecraft.item.ItemStack;
import rsstats.items.MiscItems;
import rsstats.items.SkillItem;
import rsstats.items.StatItem;
import rsstats.utils.Utils;

import java.util.Collection;
import java.util.HashMap;

/**
 * Менеджер прокачки персонажа. Отвечает за увеличечие и уменьшение статов и скиллов,
 * списание и возврат очков прокачки и за отмену изменений прокачки.
 */
public class LevelupManager {
    private ExtendedPlayer player;

    /** Хранилище информации о прокаке для каждой статы и навыка */
    protected UpgradeHistoryStorage upgradeHistory;

    /** Количество очков прокачки, которые следует вернуть игроку, если тот решил отменить прокачку */
    private int wastedPoints = 0;

    public LevelupManager(ExtendedPlayer player) {
        this.player = player;
        this.upgradeHistory = new UpgradeHistoryStorage(player);
    }

    /**
     * Вызывается при попытке игрока прокачать стату на 1 уровнень
     * @param statStack Стак со статой, который прокачивает игрок
     */
    public void statUp(ItemStack statStack) {
        UpgradeHistoryUnit unit = upgradeHistory.get(statStack);

        if (unit.canUpgrade()) {
            int price = getUpgradePrice(unit);
            if (!Utils.removeItemStackFromInventory(player.getEntityPlayer().inventory, "item.ExpItem", price))
                return;

            unit.incrementStatLevel(price);
            wastedPoints += price;
            player.updateParams(); // Пересчитваем параметры
        }
    }

    /**
     * Вызывается при попытке игрока понизить стату на 1 уровень
     * @param statStack Стак со статой, который игрок
     */
    public void statDown(ItemStack statStack) {
        UpgradeHistoryUnit unit = upgradeHistory.get(statStack);

        if (unit.canDowngrade()) { // Игрок в режиме прокачки - пытается понизить стату/навык
            int refund = unit.decrementStatLevel();
            doRefund(refund);
            player.updateParams(); // Пересчитваем параметры
        }
    }

    /**
     * Восстанавливает прокачку персонажа
     */
    public void restoreBild() {
        for (UpgradeHistoryUnit unit : upgradeHistory.getAll()) unit.restore();
        doRefund(wastedPoints);
        player.updateParams();
    }

    /* Думаю, что задавать кастомные правила прокачки через создание наследников LevelupManager
     * будет гораздо лучше, чем создание метода StatItem#getUpgradePrice(...), т.к. первый способ
     * не навязывае входные параметры, которых может быть недостаточно для реализации кастомных правил. */
    /**
     * Вычисляет стоимость прокачки статы или навыка на 1 пункт
     * @return Стоимость прокачки статы или навыка на 1 пункт
     * @throws IllegalStateException Если стата уже имеет максимальный уровень
     */
    protected int getUpgradePrice(UpgradeHistoryUnit unit) {  // TODO: Unit-test this
        if (!unit.canUpgrade()) throw new IllegalStateException("Stat already have max lvl");


        int price = 1; // Цена прокачки по-умолчанию
        if (unit.statStack.getItem() instanceof SkillItem) {
            ItemStack parentStatStack = player.statsInventory.getStat(((SkillItem) unit.statStack.getItem()).parentStat.getUnlocalizedName());
            int parentStatLevel = parentStatStack.getItemDamage();
            if (unit.getCurrentLvl() > parentStatLevel)
                price = 2;
        } else { // instanceof StatItem ONLY
            price = 2;
        }

        return price;

    }

    /**
     * Возвращает игроку указанное количество очков прокачи и отнимает из их {@link #wastedPoints}
     * @param refund Очки прокачки, которые будут возвращены игроку
     */
    protected void doRefund(int refund) { // TODO: Unit-test this
        ItemStack expStack = new ItemStack(MiscItems.expItem, refund);
        /* addItemStackToInventory успешно работает с ситуацией, если вернутся больше чем 64 предмета.
         * Нет нужды в своих проверках. */
        this.player.getEntityPlayer().inventory.addItemStackToInventory(expStack); // TODO: А что есть нет места?
        wastedPoints -= refund;
    }

    /** Хранилище информации о прокачке для одной статы или навыка */
    private static class UpgradeHistoryUnit {
        ///** Стак до прокачки. Используется для отказа изменений. */
        //private final ItemStack savedItemStack;
        /** Стак, подвергающийся прокачке (т.е. именно его изменяет LevelupManager) */
        final ItemStack statStack; // Так же может быть ключом в UpgradeHistoryStorage
        /** Стоимость прокачки для каждого уровня в формат lvl->price. */
        private HashMap<Integer, Integer> prices = new HashMap<Integer, Integer>();
        /** Уровень, с которого начилася прокачка */
        final int startUpgradeLvl;

        public UpgradeHistoryUnit(ItemStack statStack) {
            //this.savedItemStack = statStack.copy();
            //this.startUpgradeLvl = savedItemStack.getItemDamage();

            this.startUpgradeLvl = statStack.getItemDamage();
            this.statStack = statStack;
        }

        public int getMaxLvl() {
            return statStack.getItem().getMaxDamage();
        }

        public int getCurrentLvl() {
            return statStack.getItemDamage();
        }

        public boolean canUpgrade() {
            return getCurrentLvl() < getMaxLvl();
        }

        public boolean canDowngrade() {
            return getCurrentLvl() > startUpgradeLvl;
        }

        /**
         * Увеличивает стату, которая хранится в {@link #statStack}, на 1
         * @param price Стоимость прокачки в очках
         */
        public void incrementStatLevel(int price) { // TODO: Слать исключение при дстижении предела?
            statStack.setItemDamage(getCurrentLvl() < getMaxLvl() ? getCurrentLvl() + 1 : getMaxLvl());
            prices.put(getCurrentLvl(), price);
        }

        /**
         * Уменьшает стату, которая хранится в {@link #statStack}, на 1
         * @return Количество очков прокачки, которые дожны вернуться игроку
         */
        public int decrementStatLevel() {
            int reward = prices.get(getCurrentLvl());
            prices.remove(getCurrentLvl());
            statStack.setItemDamage(getCurrentLvl() > 0 ? getCurrentLvl()-1 : 0);
            return reward;
        }

        /**
         * Восстанавливает {@link #statStack} до первоначального состояния
         */
        public void restore() {
            statStack.setItemDamage(startUpgradeLvl);
            // При необходимости можно копировать NBT из savedItemStack
        }
    }


    /**
     * Хранилище информации о статах и их прокачке
     */
    private static class UpgradeHistoryStorage {
        // private, т.к. ключ и ссылка в значении должны совпадать
        private HashMap<ItemStack, UpgradeHistoryUnit> storage = new HashMap<ItemStack, UpgradeHistoryUnit>();

        public UpgradeHistoryStorage(ExtendedPlayer player) {
            // Инициализируем историю трат для статов
            for (ItemStack itemStack : player.statsInventory.getStats())
                if (itemStack != null) add(itemStack);

            // Инициализируем историю трат для скиллов
            for (ItemStack itemStack : player.skillsInventory.getSkills())
                if (itemStack != null) add(itemStack);
        }

        private void add(ItemStack statStack) {
            if ( !(statStack.getItem() instanceof StatItem) )
                throw new IllegalArgumentException("ItemStack argument must contain an StatItem.");
            storage.put(statStack, new UpgradeHistoryUnit(statStack));
        }

        public UpgradeHistoryUnit get(ItemStack key) {
            return storage.get(key);
        }

        /**
         * @return информацию обо всех прокачиваемых навыках и статах,
         * информацией о которых расолагает {@link UpgradeHistoryStorage}
         */
        public Collection<UpgradeHistoryUnit> getAll() {
            return storage.values();
        }
    }
}
