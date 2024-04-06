package org.tbstcraft.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.CommandModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;

@QuarkCommand(name = "console", op = true)
@QuarkModule(version = "1.0.0")
public final class ConsoleCommand extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        this.getLanguage().sendMessageTo(sender, "execute", sb.toString());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), sb.toString());
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("[commandline]");
        }
    }
}
