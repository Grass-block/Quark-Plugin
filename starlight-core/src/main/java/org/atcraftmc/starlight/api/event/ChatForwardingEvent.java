package org.atcraftmc.starlight.api.event;

import net.kyori.adventure.text.Component;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

@SLEvent
public final class ChatForwardingEvent extends CustomEvent implements Cancellable {
    private final CommandSender sender;
    private final CommandSender viewer;
    private final Component previous;
    private Component examined;
    private boolean cancelled;

    public ChatForwardingEvent(CommandSender sender, CommandSender viewer, Component previous) {
        this.sender = sender;
        this.viewer = viewer;
        this.previous = previous;
        this.examined = previous;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(ChatForwardingEvent.class);
    }

    public CommandSender getSender() {
        return sender;
    }

    public CommandSender getViewer() {
        return viewer;
    }

    public Component getExamined() {
        return examined;
    }

    public void setExamined(Component examined) {
        this.examined = examined;
    }

    public Component getPrevious() {
        return previous;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isViewerConsole() {
        return !(this.viewer instanceof Player);
    }

    public boolean isSenderConsole() {
        return !(this.sender instanceof Player);
    }

    public Player getSenderAsPlayer() {
        return ((Player) getSender());
    }
}
