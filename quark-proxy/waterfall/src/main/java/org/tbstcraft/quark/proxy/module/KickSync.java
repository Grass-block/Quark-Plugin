package org.tbstcraft.quark.proxy.module;

import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.Config;

public class KickSync extends ProxyModule {

    @Override
    public void onEnable() {
        if (!Config.getSection("sync").getBoolean("sync-kick")) {
            return;
        }
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    @EventHandler
    public void onPlayerKick(ServerKickEvent event) {
        event.getPlayer().disconnect(event.getKickReasonComponent());
    }
}
