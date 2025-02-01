package org.atcraftmc.quark.commands;

import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkCommand(name = "console", permission = "-quark.console")
@QuarkModule(version = "1.0.0")
public final class ConsoleCommand extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        this.getLanguage().sendMessage(sender, "execute", sb.toString());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), sb.toString());
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        var buffer = suggestion.getBuffer();
        if (buffer.isEmpty()) {
            return;
        }

        if (buffer.size() < 1) {
            return;
        }

        var id = buffer.get(0);
        var cmd = LegacyCommandManager.getCommandMap().getCommand(id);

        if (cmd == null) {
            return;
        }

        var origin = buffer.toArray(new String[0]);

        if (origin.length < 1) {
            return;
        }

        var subArgs = new String[origin.length - 1];

        System.arraycopy(origin, 1, subArgs, 0, origin.length - 1);

        suggestion.suggest(buffer.size() - 1, cmd.tabComplete(suggestion.getSender(), id, subArgs));
    }
}
