package rsstats.common.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import rsstats.api.events.NewSessionEvent;
import rsstats.data.ExtendedPlayer;
import rsstats.data.WorldData;

import java.util.List;

public class CommandGameSession extends CommandBase {
    @Override
    public String getCommandName() {
        return "gamesession";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "commands.gamesession.usage";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) { // TODO: В mcp_ru
        if (p_71515_2_.length > 1) {

            if (p_71515_2_[0].equals("timer")) {
                if (p_71515_2_[1].equals("set")) {
                    long timer = parseIntWithMin(p_71515_1_, p_71515_2_[2], 0);
                    setTimer(p_71515_1_, timer);
                } else if (p_71515_2_[1].equals("get")) {
                    printTimer(p_71515_1_);
                } else if (p_71515_2_[1].equals("off")) {
                    disableTimer(p_71515_1_);
                }
                return;
            }

            if (p_71515_2_[0].equals("new")) {
                newSession(p_71515_1_);
                return;
            }

//            int i;
//
//            if (p_71515_2_[0].equals("set"))
//            {
//                if (p_71515_2_[1].equals("day"))
//                {
//                    i = 1000;
//                }
//                else if (p_71515_2_[1].equals("night"))
//                {
//                    i = 13000;
//                }
//                else
//                {
//                    i = parseIntWithMin(p_71515_1_, p_71515_2_[1], 0);
//                }
//
//                this.setTime(p_71515_1_, i);
//                func_152373_a(p_71515_1_, this, "commands.time.set", new Object[] {Integer.valueOf(i)});
//                return;
//            }
//
//            if (p_71515_2_[0].equals("add"))
//            {
//                i = parseIntWithMin(p_71515_1_, p_71515_2_[1], 0);
//                this.addTime(p_71515_1_, i);
//                func_152373_a(p_71515_1_, this, "commands.time.added", new Object[] {Integer.valueOf(i)});
//                return;
//            }
        }

        throw new WrongUsageException("commands.gamesession.usage", new Object[0]); // TODO: Убрать обджект
    }

    public void newSession(ICommandSender commandSender) {
        for (WorldServer worldServer : MinecraftServer.getServer().worldServers) {
            for (EntityPlayer playerEntity : (List<EntityPlayer>) worldServer.playerEntities) {
                // TODO: А не лучше ли слать один евент без привязки к игроку?
                NewSessionEvent event = new NewSessionEvent(ExtendedPlayer.get(playerEntity));
                MinecraftForge.EVENT_BUS.post(event);
            }
        }
        func_152373_a(commandSender, this, "commands.gamesession.new"/*, new Object[] {Integer.valueOf(i)}*/);
    }

    public void setTimer(ICommandSender commandSender, long timer) {
        // TODO: а если придет другой ворлд?
        WorldData.get(commandSender.getEntityWorld()).setGameSessionTimer(timer, commandSender.getEntityWorld()); // TODO: Для одного или всех миров?
        func_152373_a(commandSender, this, "commands.gamesession.timer.set"/*, new Object[] {Integer.valueOf(i)}*/);
    }

    public void disableTimer(ICommandSender commandSender) {
        WorldData.get(commandSender.getEntityWorld()).disableGameSessionTimer(); // TODO: Для одного или всех миров?
        func_152373_a(commandSender, this, "commands.gamesession.timer.off"/*, new Object[] {Integer.valueOf(i)}*/);
    }

    public void printTimer(ICommandSender commandSender) {
        WorldData worldData = WorldData.get(commandSender.getEntityWorld());
        if (worldData.isGameSessionTimerSet()) {
            func_152373_a(commandSender, this, "commands.gamesession.timer.get.notset");
        } else {
            long timer = worldData.getGameSessionTimer();
            func_152373_a(commandSender, this, "commands.gamesession.timer.get", new Object[] {timer});
        }
        // TODO: Форматировать время в соостветствии с локалью?
    }
}
