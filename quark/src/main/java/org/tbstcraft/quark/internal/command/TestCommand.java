package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.tbstcraft.quark.foundation.command.CoreCommand;

@QuarkCommand(name = "test", permission = "-quark.test")
public final class TestCommand extends CoreCommand {
    @Override
    public void suggest(CommandSuggestion suggestion) {

    }

    @Override
    public void execute(CommandExecution context) {

    }
}
