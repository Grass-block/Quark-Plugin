package org.atcraftmc.quark.contents;

import org.bukkit.event.EventHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@AutoRegister({ServiceType.EVENT_LISTEN})
@QuarkModule(version = "1.0")
public final class SitOnPlayer extends PackageModule {

    @Inject("+quark.sit")
    public Permission sitPermission;

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        boolean s = event.getPlayer().hasPermission(this.sitPermission);
        boolean t = target.hasPermission(this.sitPermission);

        if ((!s) && t) {
            return;
        }

        target.addPassenger(event.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        for (var e : event.getPlayer().getPassengers()) {
            if (!(e instanceof Player target)) {
                continue;
            }
            event.getPlayer().removePassenger(e);
            return;
        }
    }
}
