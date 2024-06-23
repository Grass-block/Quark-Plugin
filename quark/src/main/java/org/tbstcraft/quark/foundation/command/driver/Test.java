package org.tbstcraft.quark.foundation.command.driver;

import org.bukkit.command.CommandSender;

import java.util.Set;

public class Test {

    @CommandExecutor(path = "/quark config reload [name]", permission = "-quark.config.reload")
    public void reloadConfig(CommandSender sender, String[] args) {

    }

    @CommandTabCompleter(path = "/quark config reload", permission = "-quark.config.reload")
    public void reloadConfigTab(CommandSender sender, Set<String> tabList) {

    }
}
