package org.tbstcraft.quark.lobby;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.permission.PermissionService;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class MapProtect extends PackageModule {
    @Override
    public void enable() {
        PermissionService.createPermission("-quark.lobby.break");
        PermissionService.createPermission("-quark.lobby.interact");
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("quark.lobby.break")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }
        if (!event.hasItem()) {
            return;
        }
        if (event.getPlayer().hasPermission("quark.lobby.interact")) {
            return;
        }
        event.setCancelled(true);
    }
}
