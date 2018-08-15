/**
 * По аналогии с {@link net.minecraft.init.Items}, все итемы мода хранятся в статик полях.
 *
 * <p>
 *     Не рекомендуется хранить итемы в локальных переменных и наделяться, что можно получить к ним доступ
 *     через {@link cpw.mods.fml.common.registry.GameRegistry#findItem(java.lang.String, java.lang.String)},
 *     т.к. он может возвратить не тот предмет. Это особенно важно в тех случаях, если вы регистрируете свои
 *     ItemRender'ы или TESR'ы. TESR'ы нужно регистрировать ИМЕННО для того итема/блока, который был зарегистирован в
 *     {@link cpw.mods.fml.common.registry.GameRegistry}. В противном случае, TESR не будет работать, потому что
 *     findItem вернул не тот же самый предмет, который был зарегистрирован.
 * </p>
 */
package rsstats.items;