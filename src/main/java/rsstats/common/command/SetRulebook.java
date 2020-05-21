package rsstats.common.command;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import rsstats.common.RulebookRegistry;

import java.util.List;

public class SetRulebook extends CommandBase {
    @Override
    public String getCommandName() {
        return "setrulebook";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "commands.rulebook.usage.set";
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return p_71516_2_.length == 1 ? getListOfStringsMatchingLastWord(p_71516_2_, RulebookRegistry.getRulebooksNames()) : (p_71516_2_.length == 2 ? getListOfStringsMatchingLastWord(p_71516_2_, new String[] {"true", "false"}): null);
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) {
        if (args.length > 2) throw new WrongUsageException(this.getCommandUsage(commandSender));

        if (RulebookRegistry.getRulebook(args[0]) != null) {
            RulebookRegistry.activateRulebook(args[0], commandSender.getEntityWorld());
        } else {
            throw new CommandException("commands.rulebook.failure.set", args[0]);
        }

        boolean informPlayers = args.length == 2 ? Boolean.parseBoolean(args[1]) : false;
        if (informPlayers) {
            FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager()
                    .sendChatMsg(new ChatComponentTranslation("commands.rulebook.success.set"));
        } else {
            // В отличии от commandSender.addChatMessage() служит и для уведомления других администраторов
            func_152373_a(commandSender, this, "commands.rulebook.success.set");
        }
    }
}