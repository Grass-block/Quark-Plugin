package org.tbstcraft.quark.framework.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.util.Region;

@QuarkEvent
public class WorldeditSectionUpdateEvent extends CustomEvent {
    private final Player player;
    private final Region region;

    public WorldeditSectionUpdateEvent(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    @SuppressWarnings({"unused"})
    public static HandlerList getHandlerList() {
        return getHandlerList(WorldeditSectionUpdateEvent.class);
    }

    public Region getRegion() {
        return region;
    }

    public Player getPlayer() {
        return player;
    }
}
