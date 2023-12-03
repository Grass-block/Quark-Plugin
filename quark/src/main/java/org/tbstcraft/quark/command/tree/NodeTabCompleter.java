package org.tbstcraft.quark.command.tree;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface NodeTabCompleter {
    void tab(CommandSender sender, String[] args, int tabPos, List<String> tabList);
}
