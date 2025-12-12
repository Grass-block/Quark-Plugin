package org.atcraftmc.starlight.security;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.Permission;
import org.atcraftmc.starlight.migration.MessageAccessor;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.core.permission.PermissionService;

@ApplicationModule(id="advanced-permission-control",version = "1.1")
@AutoRegister(Registrations.SERVER_EVENT)
public final class AdvancedPermissionControl extends SLPackageModule {
    @Inject("+starlight.player.chat")
    public Permission chatPermission;

    @Inject("+starlight.player.interact")
    public Permission interactPermission;

    @Inject("+starlight.player.break")
    public Permission breakPermission;

    @Inject("+starlight.player.interact.entity")
    public Permission interactEntityPermission;

    private void testPermission(Cancellable event, Player player, Permission permission) {
        if (player.hasPermission(permission)) {
            return;
        }
        event.setCancelled(true);
        MessageAccessor.send(this.handle().getLanguage(), player, "no-perm", permission.getName());
    }

    @Override
    public void enable() {
        PermissionService.update();
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        testPermission(event, event.getPlayer(), this.chatPermission);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        testPermission(event, event.getPlayer(), this.interactPermission);
    }

    @EventHandler
    public void onPlayerBreak(final BlockBreakEvent event) {
        testPermission(event, event.getPlayer(), this.breakPermission);
    }

    @EventHandler
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        testPermission(event, event.getPlayer(), this.interactEntityPermission);
    }
}
