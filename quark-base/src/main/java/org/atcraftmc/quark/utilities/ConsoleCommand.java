package org.atcraftmc.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.List;

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
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("[commandline]");
        }
    }
}
