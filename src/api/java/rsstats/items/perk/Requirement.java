package rsstats.items.perk;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.StatCollector;
import rsstats.data.ExtendedPlayer;

/**
 * Класс, реализующий "требования" для перков
 */
public abstract class Requirement {
    /**
     * Определяет, удовлетворяет ли игрок данному требованию
     * @param player Игрок
     * @return True, если удовтетворяет. Иначе - false.
     */
    public abstract boolean isSuitableFor(ExtendedPlayer player);

    /**
     * @return Переведенное описание требования на языке пользователя (клиента игры)
     */
    @SideOnly(Side.CLIENT)
    public abstract String toStringTranslated();


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
            return player.getRank().compare(minRank) >= 0;
        }

        @Override
        public String toStringTranslated() {
            return StatCollector.translateToLocalFormatted("requirement.rank", minRank.getTranslatedName());
        }
    }
}
