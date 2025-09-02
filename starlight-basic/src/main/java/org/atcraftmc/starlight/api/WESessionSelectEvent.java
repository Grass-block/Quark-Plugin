package org.atcraftmc.starlight.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.objects.Region;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

@SLEvent(async = false)
public final class WESessionSelectEvent extends CustomEvent implements Cancellable {
    private final Player player;
    private final Region region;
    private boolean cancel = false;

    public WESessionSelectEvent(Player player, Region region) {
        this.player = player;
        this.region = region;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(WESessionSelectEvent.class);
    }

    public Player getPlayer() {
        return player;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
