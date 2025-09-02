package org.atcraftmc.quark.lobby;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.ConfigAccessor;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "1.0.3")
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
        if (event.getFrom().getY() > ConfigAccessor.getInt(this.getConfig(), "lowest-y")) {
            return;
        }
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation().add(0.5,0.5,0.5));
    }
}
