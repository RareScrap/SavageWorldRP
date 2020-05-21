package rsstats.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import rsstats.common.RulebookRegistry;
import ru.rarescrap.weightapi.WeightRegistry;

public class GetActiveRulebook extends CommandBase {
    @Override
    public String getCommandName() {
        return "getactiverulebook";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "commands.rulebook.usage.getActive";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args) {
        if (args.length != 0) throw new WrongUsageException(this.getCommandUsage(commandSender));

        if (RulebookRegistry.getActiveRulebook() == null) {
            commandSender.addChatMessage(new ChatComponentTranslation("commands.rulebook.failure.getActive"));
        } else {
            commandSender.addChatMessage(new ChatComponentTranslation(
                    "commands.rulebook.success.getActive",WeightRegistry.getActiveProviderName()));
        }
    }
}
