package org.atcraftmc.quark.lobby;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(version = "1.0.3")
public final class PlayerProtect extends SLPackageModule {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerFallIntoVoid(PlayerMoveEvent event) {
        if (event.getFrom().getY() > ConfigAccessor.getInt(this.config(), "lowest-y")) {
            return;
        }
        event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation().add(0.5,0.5,0.5));
    }
}
