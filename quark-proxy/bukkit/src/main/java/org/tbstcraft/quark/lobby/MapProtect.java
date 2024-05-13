package org.tbstcraft.quark.lobby;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.service.base.permission.PermissionService;

@QuarkModule(version = "1.0.0")
@EventListener
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
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.getPlayer().hasPermission("quark.lobby.interact")) {
            return;
        }
        event.setCancelled(true);
    }
}