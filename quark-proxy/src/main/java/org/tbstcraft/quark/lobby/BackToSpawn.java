package org.tbstcraft.quark.lobby;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.module.services.EventListener;

@EventListener
@QuarkModule(version = "1.0")
public final class BackToSpawn extends PackageModule {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation().add(0.5,0.5,0.5));
    }
}
