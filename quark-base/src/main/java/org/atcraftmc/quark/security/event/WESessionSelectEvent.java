package org.atcraftmc.quark.security.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.foundation.region.Region;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

@QuarkEvent(async = false)
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
