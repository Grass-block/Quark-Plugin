package org.tbstcraft.quark.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandEvent extends Event {
    public static HandlerList handlerList = new HandlerList();

    private final String command;
    private final String[] args;
    private final CommandSender sender;

    public CommandEvent(String command, String[] args, CommandSender sender) {
        this.command = command;
        this.args = args;
        this.sender = sender;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public String getCommand() {
        return command;
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

    public boolean shouldReturn(String id) {
        return !Objects.equals(id, this.getCommand());
    }
}
