package org.atcraftmc.starlight;

import org.atcraftmc.starlight.commands.CommandExec;
import org.atcraftmc.starlight.commands.EntityMotion;
import org.atcraftmc.starlight.commands.ItemCommand;
import org.atcraftmc.starlight.commands.WorldEditCommands;
import org.atcraftmc.starlight.console.*;
import org.atcraftmc.starlight.display.*;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.management.*;
import org.atcraftmc.starlight.security.*;
import org.atcraftmc.starlight.sideload.RecipeLoader;
import org.atcraftmc.starlight.utilities.*;
import org.atcraftmc.starlight.warp.BackToDeath;
import org.atcraftmc.starlight.warp.RTP;
import org.atcraftmc.starlight.warp.TPA;
import org.atcraftmc.starlight.warp.Waypoints;

import java.util.Set;

public final class SLBasicPackage extends MultiPackageProvider {

    static PackageInitializer management() {
        return PackageBuilderInitializer.of("starlight-management", FeatureAvailability.BOTH, (i) -> {
            i.module("ban", Ban.class);
            i.module("chat-filter", ChatFilter.class);
            i.module("chat-report", ChatReport.class);
            i.module("maintenance", Maintenance.class);
            i.module("mute", Mute.class); //todo [DFU] import mute status
            i.module("tps-bar", TPSBar.class);
            i.module("server-info", ServerInfo.class);
            i.module("kick-on-reload", KickOnReload.class);
            i.module("plugin-manager-command", PluginManagerCommand.class);

            i.config("starlight-management");
            i.language("starlight-management", "zh_cn");
        });
    }

    static PackageInitializer console() {
        return PackageBuilderInitializer.of("starlight-console", FeatureAvailability.BOTH, (i) -> {
            i.module("clear-console", ClearConsole.class);
            i.module("console-execute", ConsoleExecute.class);
            i.module("custom-log-format", CustomLogFormat.class);
            i.module("log-color-patch", LogColorPatch.class);
            i.module("stop-confirm", StopConfirm.class);

            i.language("starlight-console", "zh_cn");
        });
    }

    static PackageInitializer warps() {
        return PackageBuilderInitializer.of("starlight-warps", FeatureAvailability.BOTH, (i) -> {
            i.module("waypoint", Waypoints.class); //todo [DFU] import waypoints
            i.module("rtp", RTP.class);
            i.module("tpa", TPA.class);
            i.module("back-to-death", BackToDeath.class);

            i.config("starlight-warps");
            i.language("starlight-warps", "zh_cn");
        });
    }

    static PackageInitializer utilities() {
        return PackageBuilderInitializer.of("starlight-utilities", FeatureAvailability.BOTH, (i) -> {
            i.module("calculator", Calculator.class);
            i.module("chat-at", ChatAt.class);
            i.module("dynamic-view-distance", DynamicViewDistance.class); //todo [DFU] miss
            i.module("player-position-lock", PlayerPositionLock.class);
            i.module("position-align", PositionAlign.class);
            i.module("chat-components", ChatComponent.class);
            i.module("surrounding-refresh", SurroundingRefresh.class);

            i.config("starlight-utilities");
            i.language("starlight-utilities", "zh_cn");
        });
    }

    static PackageInitializer commands() {
        return PackageBuilderInitializer.of("starlight-commands", FeatureAvailability.BOTH, (i) -> {
            i.module("command-exec", CommandExec.class);
            i.module("entity-motion", EntityMotion.class);
            i.module("item-command", ItemCommand.class);
            i.module("worldedit-commands", WorldEditCommands.class);

            i.language("starlight-commands", "zh_cn");
        });
    }

    static PackageInitializer security() {
        return PackageBuilderInitializer.of("starlight-security", FeatureAvailability.BOTH, (i) -> {
            i.module("explosion-defender", ExplosionDefender.class); //todo [DFU] import whitelist region
            i.module("img-regulation-sync", IMGRegulationSync.class);
            i.module("ip-defender", IPDefender.class); //todo [DFU] miss
            i.module("permission-manager", PermissionManager.class); //todo [DFU] import permission data

            i.service(WESessionTrackService.class);

            i.config("starlight-security");
            i.language("starlight-security", "zh_cn");
        });
    }

    static PackageInitializer display() {
        return PackageBuilderInitializer.of("starlight-display", FeatureAvailability.BOTH, (i) -> {
            i.module("action-bar-hud", ActionBarHUD.class);
            i.module("afk", AFK.class);
            i.module("chat-format", ChatFormat.class);
            i.module("custom-death-message", CustomDeathMessage.class);
            i.module("custom-motd", CustomMotd.class);
            i.module("custom-scoreboard", CustomScoreboard.class);
            i.module("drop-item-info", DropItemInfo.class);
            i.module("hover-display", HoverDisplay.class); //todo [DFU] import hover data
            i.module("player-name-header", PlayerNameHeader.class); //todo [DFU] import header
            i.module("tab-menu", TabMenu.class);
            i.module("welcome-message", WelcomeMessage.class); //todo [DFU] import status-> first-join-detection
            i.module("we-session-renderer", WESessionRenderer.class);

            i.service(VisualScoreboardService.class);
            i.service(PlayerWelcomeService.class);

            i.config("starlight-display");
            i.language("starlight-display", "zh_cn");
        });
    }

    static PackageInitializer sideload() {
        return PackageBuilderInitializer.of("starlight-sideload", FeatureAvailability.BOTH, (i) -> {
            i.module("recipe-loader", RecipeLoader.class);
        });
    }


    static Set<PackageInitializer> initializers() {
        return Set.of(management(), console(), warps(), utilities(), display(), security(), sideload(), commands());
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }
}
