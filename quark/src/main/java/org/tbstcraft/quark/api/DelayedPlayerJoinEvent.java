package org.tbstcraft.quark.api;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

@QuarkEvent(async = false)
public final class DelayedPlayerJoinEvent extends CustomEvent {
    private final Player player;

    public DelayedPlayerJoinEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(DelayedPlayerJoinEvent.class);
    }

    public Player getPlayer() {
        return player;
    }
}
