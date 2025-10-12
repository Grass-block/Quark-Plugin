package org.atcraftmc.starlight.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.CachedInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.List;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(id = "position-lock", version = "1.0.0")
@CommandProvider(PositionLock.LockPositionCommand.class)
public final class PositionLock extends SLPackageModule {
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
            MessageAccessor.send(this.handle().getLanguage(), p, "unlock");
        } else {
            this.lockedPlayers.add(name);
            MessageAccessor.send(this.handle().getLanguage(), p, "lock");
        }
    }

    @QuarkCommand(name = "lock-position", permission = "+starlight.command.lock")
    public static final class LockPositionCommand extends ModuleCommand<PositionLock> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 0) {
                if (!sender.isOp()) {
                    this.sendPermissionMessage(sender, "(ServerOperator)");
                    return;
                }

                this.getModule().toggle(args[0]);
                if (this.getModule().isLocked(args[0])) {
                    MessageAccessor.send(this.getLanguage(), sender, "lock-player", args[0]);
                } else {
                    MessageAccessor.send(this.getLanguage(), sender, "unlock-player", args[0]);
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
