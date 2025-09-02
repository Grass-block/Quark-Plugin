package org.atcraftmc.starlight.api;

import com.sk89q.worldedit.extent.Extent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

@SLEvent(async = false)
public final class WESessionEditEvent extends CustomEvent implements Cancellable {
    private final Player player;
    private Extent mask;
    private boolean cancel = false;

    public WESessionEditEvent(Player player, Extent mask) {
        this.player = player;
        this.mask = mask;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(WESessionEditEvent.class);
    }

    public Player getPlayer() {
        return player;
    }

    public Extent getMask() {
        return this.mask;
    }

    public void setMask(Extent mask) {
        this.mask = mask;
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