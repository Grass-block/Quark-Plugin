package org.atcraftmc.starlight.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

import java.util.List;

@SLEvent
public final class CommandTabEvent extends CustomEvent implements Cancellable {
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
        return getHandlerList(CommandTabEvent.class);
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

    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
