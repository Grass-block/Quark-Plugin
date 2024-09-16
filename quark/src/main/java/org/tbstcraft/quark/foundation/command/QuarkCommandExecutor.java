package org.tbstcraft.quark.foundation.command;

import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.execute.CommandExecutor;
import org.tbstcraft.quark.Quark;

public interface QuarkCommandExecutor extends CommandExecutor {
    @Override
    default CommandManager getHandle(){
        return Quark.getInstance().getCommandManager();
    }
}
