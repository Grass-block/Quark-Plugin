package org.tbstcraft.quark.utilities;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.CachedInfo;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.util.HashSet;
import java.util.List;

@EventListener
@QuarkModule(version = "1.0.0")
@CommandRegistry(PlayerPositionLock.LockPositionCommand.class)
public class PlayerPositionLock extends PackageModule {
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
        Player p = PlayerUtil.strictFindPlayer(name);
        if (p == null) {
            return;
        }
        if (this.isLocked(name)) {
            this.lockedPlayers.remove(name);
            this.getLanguage().sendMessageTo(p, "unlock");
        } else {
            this.lockedPlayers.add(name);
            this.getLanguage().sendMessageTo(p, "lock");
        }
    }

    @QuarkCommand(name = "lock-position")
    public static final class LockPositionCommand extends ModuleCommand<PlayerPositionLock> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 0) {
                if (!sender.isOp()) {
                    this.sendPermissionMessage(sender);
                    return;
                }

                this.getModule().toggle(args[0]);
                if (this.getModule().isLocked(args[0])) {
                    this.getLanguage().sendMessageTo(sender, "lock-player", args[0]);
                } else {
                    this.getLanguage().sendMessageTo(sender, "unlock-player", args[0]);
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
