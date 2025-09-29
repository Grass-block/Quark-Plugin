package org.atcraftmc.quark.lobby;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.gb2022.modular.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;
import org.atcraftmc.starlight.core.permission.PermissionService;

@ApplicationModule(version = "1.0.3")
@AutoRegister(Registrations.SERVER_EVENT)
@ComponentProvider(MapProtect.PaperPreAttackEventEXT.class)
public final class MapProtect extends SLPackageModule {
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


    @AutoRegister(Registrations.SERVER_EVENT)
    public static final class PaperPreAttackEventEXT extends SLModuleComponent<MapProtect> {
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
