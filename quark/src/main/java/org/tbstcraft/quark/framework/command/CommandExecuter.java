package org.tbstcraft.quark.framework.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandExecuter {


    default boolean onTab(CommandSender sender, String[] buffer, List<String> tabList) {
        this.onCommandTab(sender, buffer, tabList);
        return true;
    }

    default boolean execute(CommandSender sender, String[] args) {
        this.onCommand(sender, args);
        return true;
    }

    default void onCommand(CommandSender sender, String[] args) {
    }

    default void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
    }
}
