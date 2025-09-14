package org.atcraftmc.starlight.api;

import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SLEvent
public final class PlayerFirstJoinEvent extends CustomEvent {
    private final Player player;

    public PlayerFirstJoinEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(PlayerFirstJoinEvent.class);
    }

    public Player getPlayer() {
        return player;
    }
}
