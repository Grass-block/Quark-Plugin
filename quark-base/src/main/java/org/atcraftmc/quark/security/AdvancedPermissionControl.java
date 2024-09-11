package org.atcraftmc.quark.security;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.permission.PermissionService;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class AdvancedPermissionControl extends PackageModule {
    @Inject("+quark.player.chat")
    public Permission chatPermission;

    @Inject("+quark.player.interact")
    public Permission interactPermission;

    @Inject("+quark.player.break")
    public Permission breakPermission;

    @Inject("+quark.player.interactentity")
    public Permission interactEntityPermission;

    private void testPermission(Cancellable event, Player player, Permission permission) {
        if (player.hasPermission(permission)) {
            return;
        }
        event.setCancelled(true);
        getLanguage().sendMessage(player, "no-perm", permission.getName());
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
