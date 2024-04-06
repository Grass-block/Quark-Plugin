package org.tbstcraft.quark.command.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CommandTabEvent extends Event implements Cancellable {
    public static final HandlerList handlerList = new HandlerList();
    private final CommandSender sender;
    private final String[] args;
    private final List<String> completions;
    private final String commandLine;
    private boolean cancelled = false;

    public CommandTabEvent(CommandSender sender, String commandLine, String[] args, List<String> completions) {
        this.sender = sender;
        this.args = args;
        this.completions = completions;
        this.commandLine = commandLine;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public CommandSender getSender() {
        return sender;
    }

    public String[] getArgs() {
        return args;
    }

    public List<String> getCompletions() {
        return completions;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
