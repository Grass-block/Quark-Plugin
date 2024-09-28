package org.atcraftmc.quark.security.event;

import com.sk89q.worldedit.EditSession;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.tbstcraft.quark.foundation.region.Region;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

@QuarkEvent(async = false)
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