package rsstats.common.command;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import rsstats.common.RulebookRegistry;

public class ClearRulebook extends CommandBase {

    @Override
    public String getCommandName() {
        return "clearrulebook";
    } // TODO: Лучше "rulebook clear"

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {

    }

    public static class Message implements IMessage {
        @Override public void fromBytes(ByteBuf buf) {}
        @Override public void toBytes(ByteBuf buf) {}
    }

    public static class MessageHandler implements IMessageHandler<ClearRulebook.Message, IMessage> {
        @Override
        public IMessage onMessage(ClearRulebook.Message msg, MessageContext ctx) {
            RulebookRegistry.applyToClient(null);
            return null;
        }
    }
}
