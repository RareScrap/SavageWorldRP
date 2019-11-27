package rsstats.i18n;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Интерфейс, отвечающий за возможность производить локализацию на клиенте
 */
public interface IClientTranslatable {
    /**
     * @return локализированную на книенте строку
     */
    @SideOnly(Side.CLIENT)
    String getTranslatedString();
}
