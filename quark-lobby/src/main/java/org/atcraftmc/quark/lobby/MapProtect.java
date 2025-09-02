package org.atcraftmc.quark.lobby;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.permission.PermissionService;

@SLModule(version = "1.0.3")
@AutoRegister(ServiceType.EVENT_LISTEN)
@Components(MapProtect.PaperPreAttackEventEXT.class)
public final class MapProtect extends PackageModule {
    private static boolean allowBreak(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        if (player.hasPermission("quark.lobby.break")) {
            return true;
        }

        return false;
    }

    @Override
    public void enable() {
        PermissionService.createPermission("!quark.lobby.break");
        PermissionService.createPermission("!quark.lobby.interact");
    }

    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) {
        if (allowBreak(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            return;
        }
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        if (allowBreak(player)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.FARMLAND) {
                event.setCancelled(true);
                return;
            }
        }

        if (!event.hasBlock()) {
            return;
        }
        if (!event.hasItem()) {
            return;
        }

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (event.getPlayer().hasPermission("quark.lobby.interact")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            return;
        }

        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (event.getPlayer().hasPermission("quark.lobby.interact")) {
            return;
        }
        event.setCancelled(true);
    }


    @AutoRegister(Registers.BUKKIT_EVENT)
    public static final class PaperPreAttackEventEXT extends ModuleComponent<MapProtect> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.PrePlayerAttackEntityEvent"));
        }

        @EventHandler
        public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
            if(event.getAttacked() instanceof Player) {
                return;
            }
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (event.getPlayer().hasPermission("quark.lobby.break")) {
                return;
            }
            event.setCancelled(true);
        }
    }
}
