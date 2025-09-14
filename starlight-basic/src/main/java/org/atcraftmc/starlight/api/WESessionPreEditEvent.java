package org.atcraftmc.starlight.api;

import com.sk89q.worldedit.EditSession;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.atcraftmc.starlight.core.Region;
import org.atcraftmc.starlight.core.event.CustomEvent;
import org.atcraftmc.starlight.core.event.SLEvent;

@SLEvent(async = false)
public final class WESessionPreEditEvent extends CustomEvent implements Cancellable {
    private final Player player;
    private final Region region;
    private boolean cancel = false;
    private final EditSession.Stage stage;

    public WESessionPreEditEvent(Player player, Region region, EditSession.Stage stage) {
        this.player = player;
        this.region = region;
        this.stage = stage;
    }

    public static HandlerList getHandlerList() {
        return getHandlerList(WESessionPreEditEvent.class);
    }

    public Player getPlayer() {
        return player;
    }

    public Region getRegion() {
        return region;
    }

    public EditSession.Stage getStage() {
        return stage;
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