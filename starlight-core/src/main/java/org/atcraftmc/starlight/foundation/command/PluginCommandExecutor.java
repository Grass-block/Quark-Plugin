package org.atcraftmc.starlight.foundation.command;

import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.execute.CommandExecutor;
import org.atcraftmc.starlight.Starlight;

public interface PluginCommandExecutor extends CommandExecutor {
    @Override
    default CommandManager getHandle(){
        return Starlight.instance().getCommandManager();
    }
}
