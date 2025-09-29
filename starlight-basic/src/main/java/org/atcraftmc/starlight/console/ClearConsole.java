package org.atcraftmc.starlight.console;

import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.starlight.framework.module.SLCommandModule;

@ApplicationModule(id = "clear-console")
@QuarkCommand(name = "clear-console", aliases = "cls", permission = "-starlight.console.clear")
public final class ClearConsole extends SLCommandModule {
    @Override
    public void execute(CommandExecution context) {
        System.out.println("\033[H\033[J");
    }
}
