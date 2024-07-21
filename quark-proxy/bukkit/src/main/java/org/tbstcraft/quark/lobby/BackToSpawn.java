package org.tbstcraft.quark.lobby;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0")
public final class BackToSpawn extends PackageModule {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation().add(0.5,0.5,0.5));
    }
}
