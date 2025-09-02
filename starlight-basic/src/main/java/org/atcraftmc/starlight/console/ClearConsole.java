package org.atcraftmc.starlight.console;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule
@QuarkCommand(name = "clear-console",aliases = "cls",permission = "-starlight.console.clear")
public final class ClearConsole extends CommandModule {
    @Override
    public void execute(CommandExecution context) {
        System.out.println("\033[H\033[J");
    }
}
