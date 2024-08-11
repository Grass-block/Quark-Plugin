package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.HashSet;
import java.util.List;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0")
@CommandProvider(PlayerPositionLock.LockPositionCommand.class)
public final class PlayerPositionLock extends PackageModule {
    private final HashSet<String> lockedPlayers = new HashSet<>();


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (this.lockedPlayers.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    private boolean isLocked(String name) {
        return this.lockedPlayers.contains(name);
    }

    private void toggle(String name) {
        Player p = Bukkit.getPlayerExact(name);
        if (p == null) {
            return;
        }
        if (this.isLocked(name)) {
            this.lockedPlayers.remove(name);
            this.getLanguage().sendMessage(p, "unlock");
        } else {
            this.lockedPlayers.add(name);
            this.getLanguage().sendMessage(p, "lock");
        }
    }

    @QuarkCommand(name = "lock-position",permission = "+quark.lock")
    public static final class LockPositionCommand extends ModuleCommand<PlayerPositionLock> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 0) {
                if (!sender.isOp()) {
                    this.sendPermissionMessage(sender, "(ServerOperator)");
                    return;
                }

                this.getModule().toggle(args[0]);
                if (this.getModule().isLocked(args[0])) {
                    this.getLanguage().sendMessage(sender, "lock-player", args[0]);
                } else {
                    this.getLanguage().sendMessage(sender, "unlock-player", args[0]);
                }
                return;
            }

            if (sender instanceof ConsoleCommandSender) {
                this.sendPlayerOnlyMessage(sender);
            }
            this.getModule().toggle(sender.getName());
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            tabList.addAll(CachedInfo.getOnlinePlayerNames());
        }
    }
}
