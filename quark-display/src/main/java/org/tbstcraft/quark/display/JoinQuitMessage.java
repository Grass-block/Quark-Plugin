package org.tbstcraft.quark.display;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

@QuarkModule
public final class JoinQuitMessage extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.getLanguage().broadcastMessage(false, "join", event.getPlayer().getName());
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.getLanguage().broadcastMessage(false, "leave", event.getPlayer().getName());
        event.setQuitMessage(null);
    }
}
