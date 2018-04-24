package rsstats.common.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import rsstats.common.CommonProxy;
import rsstats.common.network.PacketCommandReponse;
import rsstats.data.ExtendedPlayer;

import java.util.List;

public class ParamsPlayer implements ICommand {
    private static final String PARAMS_MESSAGE_LOCALE_KEY = "command.paramsPlayer";
    private String commandName = "params";

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return commandName + " <player nickname>";
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] argString) {
        World world = sender.getEntityWorld();


        if (!world.isRemote)  {
            if (argString.length == 0)  {
                sender.addChatMessage(new ChatComponentText("Invalid argument"));
                return;
            }

            //ExtendedPlayer player =  ExtendedPlayer.get((EntityPlayer) sender.getEntityWorld().playerEntities.get(0));
            for (Object playerEntity : sender.getEntityWorld().playerEntities) {
                if (((EntityPlayer) playerEntity).getDisplayName().equals(argString[0])) {
                    int[] armor = new int[4];

                    for (int i = 0; i < 4; i++) {
                        ItemStack armorStack = ((EntityPlayer) playerEntity).getCurrentArmor(i);
                        if (armorStack != null) {
                            armor[i] = ((ItemArmor) armorStack.getItem()).damageReduceAmount;
                        }
                    }

                    CommonProxy.INSTANCE.sendTo(new PacketCommandReponse(PARAMS_MESSAGE_LOCALE_KEY, 200,
                            ((EntityPlayer) playerEntity).getDisplayName(),
                            String.valueOf(ExtendedPlayer.get((EntityPlayer) playerEntity).getStep()),
                            String.valueOf(ExtendedPlayer.get((EntityPlayer) playerEntity).getProtection()),
                            String.valueOf(ExtendedPlayer.get((EntityPlayer) playerEntity).getPersistence()),
                            String.valueOf(ExtendedPlayer.get((EntityPlayer) playerEntity).getCharisma()),
                            String.valueOf(armor[3]),
                            String.valueOf(armor[2]),
                            String.valueOf(armor[1]),
                            String.valueOf(armor[0])
                    ), (EntityPlayerMP) playerEntity);
                    //String debugTemplate = "Параметры %1$s: Шаг: %2$d, Защита %3$d, Стойкость: %4$d, Харизма:%5$d (броня: голова %6$d, торс %7$d, ноги %8$d, ступни %9$d)";
                    /*sender.addChatMessage(new ChatComponentText(String.format(
                            /*debugTemplate, *//*fStatCollector.translateToLocal(PARAMS_MESSAGE_LOCALE_KEY),
                            ((EntityPlayer) playerEntity).getDisplayName(),
                            ExtendedPlayer.get((EntityPlayer) playerEntity).getStep(),
                            ExtendedPlayer.get((EntityPlayer) playerEntity).getProtection(),
                            ExtendedPlayer.get((EntityPlayer) playerEntity).getPersistence(),
                            ExtendedPlayer.get((EntityPlayer) playerEntity).getCharisma(),
                            armor[3],
                            armor[2],
                            armor[1],
                            armor[0]
                    )));*/
                    return;
                }
            }
            sender.addChatMessage(new ChatComponentText("user not found"));
        }
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     *
     * @param p_71519_1_
     */
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     *
     * @param p_71516_1_
     * @param p_71516_2_
     */
    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return null;
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     *
     * @param p_82358_1_
     * @param p_82358_2_
     */
    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
        return false;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
