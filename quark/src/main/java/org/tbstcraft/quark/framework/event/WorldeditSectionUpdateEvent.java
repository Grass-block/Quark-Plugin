package org.tbstcraft.quark.framework.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.util.region.SimpleRegion;

@QuarkEvent(async = false)
public class WorldeditSectionUpdateEvent extends CustomEvent {
    private final Player player;
    private final SimpleRegion region;

    public WorldeditSectionUpdateEvent(Player player, SimpleRegion region) {
        this.player = player;
        this.region = region;
    }

    @SuppressWarnings({"unused"})
    public static HandlerList getHandlerList() {
        return getHandlerList(WorldeditSectionUpdateEvent.class);
    }

    public SimpleRegion getRegion() {
        return region;
    }

    public Player getPlayer() {
        return player;
    }
}
