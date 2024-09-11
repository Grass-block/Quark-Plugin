package org.atcraftmc.quark.lobby;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.3")
public final class PlayerProtect extends PackageModule {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerFallIntoVoid(PlayerMoveEvent event) {
        if (event.getFrom().getY() > this.getConfig().getInt("lowest-y")) {
            return;
        }
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation().add(0.5,0.5,0.5));
    }
}
