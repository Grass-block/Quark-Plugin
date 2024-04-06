package org.tbstcraft.quark.command.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class CommandEvent extends Event implements Cancellable {
    public static final HandlerList handlerList = new HandlerList();
    private final CommandSender sender;
    private final String name;
    private final String[] args;
    private boolean cancelled = false;

    public CommandEvent(CommandSender sender, String name, String[] args) {
        this.sender = sender;
        this.name = name;
        this.args = args;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public String getName() {
        return name;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
