package rsstats.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import rsstats.data.ExtendedPlayer;

import java.util.List;

public class AddExp extends CommandBase {
    @Override
    public String getCommandName() {
        return "addexp";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "commands.addexp.usage";
    }
    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return p_71516_2_.length == 2 ? getListOfStringsMatchingLastWord(p_71516_2_, getListOfPlayerUsernames()) : null;
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) {
        if (args.length > 2 || args.length < 1) throw new WrongUsageException(getCommandUsage(commandSender));

        int expPoints = parseIntWithMin(commandSender, args[0], 1);
        EntityPlayerMP entityPlayer = args.length >= 2 ? getPlayer(commandSender, args[1]) : getCommandSenderAsPlayer(commandSender);
        ExtendedPlayer player = ExtendedPlayer.get(entityPlayer);
        commandSender.addChatMessage(new ChatComponentText("Not implemented yet"));
    }

    /**
     * Returns String array containing all player usernames in the server.
     */
    private String[] getListOfPlayerUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }
}
