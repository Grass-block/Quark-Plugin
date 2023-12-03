package org.tbstcraft.quark.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class CommandTabEvent extends CommandEvent {
    public static HandlerList handlerList = new HandlerList();
    private final ArrayList<String> tabList = new ArrayList<>();

    public CommandTabEvent(String command, String[] args, CommandSender sender) {
        super(command, args, sender);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public void addTabItem(String item) {
        this.tabList.add(item);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public ArrayList<String> getTabList() {
        return tabList;
    }
}
