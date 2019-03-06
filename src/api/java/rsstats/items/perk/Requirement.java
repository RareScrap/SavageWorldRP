package rsstats.items.perk;

import net.minecraft.util.StatCollector;
import rsstats.data.ExtendedPlayer;
import rsstats.i18n.IClientTranslatable;

/**
 * Класс, реализующий "требования" для перков
 */
public abstract class Requirement implements IClientTranslatable {
    /**
     * Определяет, удовлетворяет ли игрок данному требованию
     * @param player Игрок
     * @return True, если удовтетворяет. Иначе - false.
     */
    public abstract boolean isSuitableFor(ExtendedPlayer player);



    /**
     * Требование к рангу игрока
     */
    public static class Rank extends Requirement {
        private ExtendedPlayer.Rank minRank;

        public Rank(ExtendedPlayer.Rank minRank) {
            this.minRank = minRank;
        }

        @Override
        public boolean isSuitableFor(ExtendedPlayer player) {
            return player.rank.compareTo(minRank) >= 0;
        }

        /**
         * @return Переведенное описание требования на языке пользователя (клиента игры)
         */
        @Override
        public String getTranslatedString() {
            return StatCollector.translateToLocalFormatted("requirement.rank", minRank.getTranslatedString());
        }
    }
}
