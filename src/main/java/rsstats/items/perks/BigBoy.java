package rsstats.items.perks;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.StatItems;
import rsstats.api.items.perk.IModifierDependent;
import rsstats.api.items.perk.PerkItem;
import rsstats.api.roll.RollModifier;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;

import java.util.List;
import java.util.Map;

import static rsstats.data.ExtendedPlayer.WEIGHT_MULTIPLIER;
import static rsstats.utils.LangUtils.and;
import static rsstats.utils.LangUtils.getLocalizedName;

public class BigBoy extends PerkItem {

    private static final AttributeModifier BIGBOY_WEIGHT_MODIFIER = new AttributeModifier("BigBoy", 1.5D, 0);

    public BigBoy() {
        setUnlocalizedName("BigBoyPerkItem");
        setTextureName(RSStats.MODID + ":perks/bigboy");
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(ExtendedPlayer.Rank.NOVICE.getTranslatedString());
        list.add(getLocalizedName(StatItems.strenghtStatItem) + " d6+ " + and()
                + " " + getLocalizedName(StatItems.enduranceStatItem) + " d6+");
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return super.isSuitableFor(player)
                && player.getStatLvl(StatItems.strenghtStatItem) >= 2
                && player.getStatLvl(StatItems.enduranceStatItem) >= 2;

    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        Map<IModifierDependent, RollModifier> modifiers = super.getModifiers();
        modifiers.put(ExtendedPlayer.ParamKeys.PERSISTENCE, new RollModifier(+1, getUnlocalizedName()+".name"));
        // TODO: carry weight modifier
        return modifiers;
    }

    @Override
    public void onAdd(StackAddToTabEvent.Pre e) {
        e.entityPlayer.getEntityAttribute(WEIGHT_MULTIPLIER).applyModifier(BIGBOY_WEIGHT_MODIFIER);
        super.onAdd(e);
    }

    @Override
    public void onRemove(StackAddToTabEvent.Pre e) {
        e.entityPlayer.getEntityAttribute(WEIGHT_MULTIPLIER).removeModifier(BIGBOY_WEIGHT_MODIFIER);
        super.onRemove(e);
    }
}
