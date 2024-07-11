package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.List;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(DynamicViewDistance.ViewDistanceCommand.class)
@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.BUKKIT, APIProfile.ARCLIGHT, APIProfile.SPIGOT})
public class DynamicViewDistance extends PackageModule {

    @Inject("-quark.viewdistance.other")
    private Permission setOtherPermission;

    @Override
    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            resetCustomViewDistance(p);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        resetCustomViewDistance(event.getPlayer());
    }

    private int setCustomViewDistance(Player player, int dist) {
        dist = Math.max(2, Math.min(dist, this.getConfig().getInt("max")));
        PlayerUtil.setViewDistance(player, dist);
        PlayerUtil.setSendViewDistance(player, dist);
        this.getLanguage().sendMessage(player, "set-target", dist);
        return dist;
    }

    private int resetCustomViewDistance(Player player) {
        int dist = player.getWorld().getViewDistance();
        PlayerUtil.setViewDistance(player, dist);
        PlayerUtil.setSendViewDistance(player, player.getWorld().getSendViewDistance());
        this.getLanguage().sendMessage(player, "set-target", dist);
        return dist;
    }

    @QuarkCommand(name = "view-distance", permission = "+quark.viewdistance")
    public static final class ViewDistanceCommand extends ModuleCommand<DynamicViewDistance> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            Player player = PlayerUtil.strictFindPlayer(args[0]);
            if (player == null) {
                getLanguage().sendMessage(sender, "player-not-exist");
                return;
            }
            if (Objects.equals(args[1], "unset")) {
                int val = this.getModule().resetCustomViewDistance(player);
                getLanguage().sendMessage(sender, "unset", args[0], val);
                return;
            }
            if (sender.hasPermission(this.getModule().setOtherPermission) && !Objects.equals(args[0], sender.getName())) {
                sendPermissionMessage(sender, "(ServerOperator)");
                return;
            }

            int val = this.getModule().setCustomViewDistance(player, Integer.parseInt(args[1]));
            getLanguage().sendMessage(sender, "set", args[0], val);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.addAll(CachedInfo.getOnlinePlayerNames());
            }
            if (buffer.length == 2) {
                tabList.addAll(List.of("2", "4", "8", "16", "24", "32", "unset"));
            }
        }
    }
}
