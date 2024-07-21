package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.container.CachedInfo;
import org.tbstcraft.quark.utilities.viewdistance.CustomSettingStrategy;
import org.tbstcraft.quark.utilities.viewdistance.PlayerCountStrategy;
import org.tbstcraft.quark.utilities.viewdistance.ViewDistanceStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(DynamicViewDistance.ViewDistanceCommand.class)
@QuarkModule(version = "1.0.0", compatBlackList = {APIProfile.BUKKIT, APIProfile.ARCLIGHT, APIProfile.SPIGOT})
public final class DynamicViewDistance extends PackageModule {
    private final List<ViewDistanceStrategy> pipeline = new ArrayList<>();

    @Inject("-quark.viewdistance.other")
    private Permission setOtherPermission;

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));

        //this.pipeline.add(new NetworkPingStrategy());
        this.pipeline.add(new PlayerCountStrategy());
        this.pipeline.add(new CustomSettingStrategy());

        TaskService.timerTask("view-distance:calc", 0, 20, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                calculatePlayerViewDistance(p, false, false);
            }
        });
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void checkCompatibility() throws Throwable {
        Player.class.getMethod("setSendViewDistance", int.class);
        Player.class.getMethod("setViewDistance", int.class);
        Player.class.getMethod("getLastLogin");
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));

        TaskService.cancelTask("view-distance:calc");

        int val = Bukkit.getViewDistance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setViewDistance(val);
            p.setSendViewDistance(val);
        }
    }

    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        TaskService.laterTask(10, () -> this.calculatePlayerViewDistance(event.getPlayer(), true, false));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        int val = Bukkit.getViewDistance();
        event.getPlayer().setViewDistance(val);
        event.getPlayer().setSendViewDistance(val);
    }


    public void calculatePlayerViewDistance(final Player player, boolean remind, boolean forceRemind) {
        int value = Bukkit.getViewDistance();
        boolean programRemind = false;

        for (ViewDistanceStrategy strategy : pipeline) {
            value = strategy.determine(player, value);
            programRemind = strategy.remindPlayer(player, programRemind);
        }

        value = Math.max(2, Math.min(32, value));

        if ((programRemind && remind) || forceRemind) {
            this.getLanguage().sendMessage(player, "set-self", value);
        }

        player.setViewDistance(value);
        player.setSendViewDistance(value);
    }

    @QuarkCommand(name = "view-distance", permission = "+quark.viewdistance")
    public static final class ViewDistanceCommand extends ModuleCommand<DynamicViewDistance> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            int value = Objects.equals(args[0], "reset") ? -1 : Integer.parseInt(args[0]);

            Player player;

            if (args.length > 1) {
                if (!sender.hasPermission(this.getModule().setOtherPermission)) {
                    sendPermissionMessage(sender, "-quark.viewdistance.other");
                    return;
                }

                player = PlayerUtil.strictFindPlayer(args[1]);
            } else {
                if (!(sender instanceof Player p)) {
                    this.sendPlayerOnlyMessage(sender);
                    return;
                }
                player = p;
            }

            if (player == null) {
                getLanguage().sendMessage(sender, "player-not-exist");
                return;
            }

            boolean isSelf = sender.getName().equals(player.getName());

            if (value == -1) {
                CustomSettingStrategy.clear(player);
                if (!isSelf) {
                    getLanguage().sendMessage(sender, "reset-target", args[0]);
                }
                getLanguage().sendMessage(player, "reset-self");

                this.getModule().calculatePlayerViewDistance(player, false, false);
                return;
            }

            value = Math.max(2, Math.min(32, value));

            CustomSettingStrategy.set(player, value);
            if (!isSelf) {
                getLanguage().sendMessage(sender, "set-target", args[0], value);
            }
            getLanguage().sendMessage(player, "set-self", value);

            this.getModule().calculatePlayerViewDistance(player, false, false);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.addAll(List.of("2", "4", "8", "16", "24", "32", "reset"));
            }
            if (buffer.length == 2) {
                tabList.addAll(CachedInfo.getOnlinePlayerNames());
            }
        }
    }
}
