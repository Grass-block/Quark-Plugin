package org.tbstcraft.quark.proxy.syncs;

import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.Sync;

public class KickSync extends Sync {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    @EventHandler
    public void onPlayerKick(ServerKickEvent event) {
        event.getPlayer().disconnect(event.getKickReasonComponent());
    }
}
