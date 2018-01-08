package rsstats.common.command;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import rsstats.common.RSStats;
import rsstats.data.ExtendedPlayer;

import java.util.List;

public class AddLevel implements ICommand {
    private static final String ADDLEVEL_MESSAGE_SUCCESS_LOCALE_KEY = "command.addLevel.success";
    private static final String ADDLEVEL_MESSAGE_ERROR_LOCALE_KEY = "command.addLevel.error";
    private String commandName = "addlevel";

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return commandName + " <player nickname>"; // liza
    }

    @Override
    public List getCommandAliases() {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] argString) {
        World world = sender.getEntityWorld();


        if (world.isRemote)

        {

            System.out.println("Not processing on Client side");

        } else

        {

            System.out.println("Processing on Server side");

            if (argString.length == 0)

            {

                sender.addChatMessage(new ChatComponentText("Invalid argument"));

                return;

            }

            EntityPlayer entityPlayer = (EntityPlayer) sender.getEntityWorld().playerEntities.get(0);
            ExtendedPlayer player =  ExtendedPlayer.get(entityPlayer);

            // TODO: Реролл заменить на опыт
            ItemStack exps = new ItemStack(GameRegistry.findItem(RSStats.MODID, "ExpItem"), 2);
            boolean status = entityPlayer.inventory.addItemStackToInventory(exps);
            /*if (status && entityPlayer.capabilities.isCreativeMode) {
                status = false;
            }*/
            if (status) {
                player.setLvl(player.getLvl() + 1);
                player.sync();
                String result = StatCollector.translateToLocal(ADDLEVEL_MESSAGE_SUCCESS_LOCALE_KEY);
                sender.addChatMessage(new ChatComponentText(
                        String.format(result, entityPlayer.getDisplayName(), player.getLvl())
                ));
            } else {
                String result = StatCollector.translateToLocal(ADDLEVEL_MESSAGE_ERROR_LOCALE_KEY);
                sender.addChatMessage(new ChatComponentText(result));
            }

            /*sender.addChatMessage(new ChatComponentText("Conjuring: [" + argString[0]

                    + "]"));


            String fullEntityName = RSStats.MODID + "." + argString[0];

            if (EntityList.stringToClassMapping.containsKey(fullEntityName))

            {
                List a = sender.getEntityWorld().playerEntities;
                EntityPlayer conjuredEntity = (EntityPlayer) EntityList.createEntityByName(fullEntityName, world);

                ExtendedPlayer extendedPlayer = ExtendedPlayer.get(conjuredEntity);
                extendedPlayer.setLvl(extendedPlayer.getLvl()+1);

            } else

            {

                List a = sender.getEntityWorld().playerEntities;
                EntityPlayer conjuredEntity = (EntityPlayer) EntityList.createEntityByName(fullEntityName, world);
                sender.addChatMessage(new ChatComponentText("Entity not found"));

            }*/

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
