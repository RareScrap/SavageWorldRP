package rsstats.items.perks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;
import rsstats.items.PerkItems;
import rsstats.items.perk.IModifierDependent;
import rsstats.items.perk.PerkItem;
import rsstats.roll.RollModifier;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;

import java.util.List;
import java.util.Map;

import static rsstats.data.ExtendedPlayer.ParamKeys;
import static rsstats.data.ExtendedPlayer.Rank;

// TODO: Явно сделать синглтоном
public class AristocratPerk extends PerkItem {

    public AristocratPerk() {
        setUnlocalizedName("AristocratPerkItem");
        setTextureName(RSStats.MODID + ":perks/aristocrat");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean p_77624_4_) {
        list.add(Rank.NOVICE.getTranslatedString());
        list.add("");
        super.addInformation(itemStack, player, list, p_77624_4_);
    }

    @Override
    public boolean isSuitableFor(ExtendedPlayer player) {
        return player.rank.moreOrEqual(Rank.NOVICE);
    }

    @Override
    public Map<IModifierDependent, RollModifier> getModifiers() {
        Map<IModifierDependent, RollModifier> modifiers = super.getModifiers();
        modifiers.put(ParamKeys.CHARISMA, new RollModifier(+2, "item.AristocratPerkItem.name"));
        return modifiers;
    }

    @SubscribeEvent
    public void onInventoryChanged(StackAddToTabEvent.Post e) {
        // Выходим во всех случаях, кроме тех, когда аристократ ДОБАВЛЯЕТСЯ
        if (!(e.change.actualItemStack != null && e.change.actualItemStack.getItem() == this)) return;

        ExtendedPlayer player = ExtendedPlayer.get(e.entityPlayer);
        // TODO: Сделать бы otherTabsInventory.getPerkTab()
        ItemStack riches = player.getPerk(PerkItems.riches);
        if (riches == null) player.addPerk(PerkItems.riches); // Если не выйдет, значит отстанется без еще одного перка
    }
}
