package org.tbstcraft.quark.framework.event.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

@QuarkEvent
public final class CommandEvent extends CustomEvent implements Cancellable {
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
        return getHandlerList(CommandEvent.class);
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
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
