package org.tbstcraft.quark.command.tree;

import org.bukkit.command.CommandSender;

public interface NodeCommandExecutor {
    void execute(CommandSender sender, String[] args);
}
