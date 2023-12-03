package org.tbstcraft.quark.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.util.Region;

public class WorldeditSectionUpdateEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    private final Player player;
    private final Region region;

    public WorldeditSectionUpdateEvent(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public Region getRegion() {
        return region;
    }

    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
