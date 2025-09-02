package org.atcraftmc.starlight;

import org.atcraftmc.starlight._unmerged.AdvancedPluginCommand;
import org.atcraftmc.starlight.console.ClearConsole;
import org.atcraftmc.starlight.console.ConsoleExecute;
import org.atcraftmc.starlight.console.CustomLogFormat;
import org.atcraftmc.starlight.console.LogColorPatch;
import org.atcraftmc.starlight.display.*;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.management.*;
import org.atcraftmc.starlight.security.IMGRegulationSync;
import org.atcraftmc.starlight.security.IPDefender;
import org.atcraftmc.starlight.security.WESessionTrackService;
import org.atcraftmc.starlight.utilities.Calculator;
import org.atcraftmc.starlight.utilities.ChatComponent;
import org.atcraftmc.starlight.utilities.DynamicViewDistance;
import org.atcraftmc.starlight.utilities.SurroundingRefresh;
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
            i.module("mute", Mute.class);

            i.module("advanced-plugin-command", AdvancedPluginCommand.class);

            i.config("starlight-management");
            i.language("starlight-management", "zh_cn");
        });
    }

    static PackageInitializer console() {
        return PackageBuilderInitializer.of("starlight-management", FeatureAvailability.BOTH, (i) -> {
            i.module("clear-console", ClearConsole.class);
            i.module("console-execute", ConsoleExecute.class);
            i.module("custom-log-format", CustomLogFormat.class);
            i.module("log-color-patch", LogColorPatch.class);

            i.language("starlight-console", "zh_cn");
        });
    }

    static PackageInitializer warps() {
        return PackageBuilderInitializer.of("starlight-warps", FeatureAvailability.BOTH, (i) -> {
            i.module("waypoint", Waypoints.class);
            i.module("rtp", RTP.class);
            i.module("tpa", TPA.class);
            i.module("back-to-death", BackToDeath.class);

            i.config("starlight-warps");
            i.language("starlight-warps", "zh_cn");
        });
    }

    static PackageInitializer utilities() {
        return PackageBuilderInitializer.of("starlight-utilities", FeatureAvailability.BOTH, (i) -> {
            i.module("dynamic-view-distance", DynamicViewDistance.class);
            i.module("chat-components", ChatComponent.class);
            i.module("surrounding-refresh", SurroundingRefresh.class);
            i.module("calculator", Calculator.class);

            i.config("starlight-utilities");
            i.language("starlight-utilities", "zh_cn");
        });
    }

    static PackageInitializer security() {
        return PackageBuilderInitializer.of("starlight-security", FeatureAvailability.BOTH, (i) -> {
            i.module("ip-defender", IPDefender.class);
            i.module("img-regulation-sync", IMGRegulationSync.class);

            i.service(WESessionTrackService.class);

            i.config("starlight-security");
            i.language("starlight-security", "zh_cn");
        });
    }

    static PackageInitializer display() {
        return PackageBuilderInitializer.of("starlight-display", FeatureAvailability.BOTH, (i) -> {
            i.module("tab-menu", TabMenu.class);
            i.module("custom-scoreboard", CustomScoreboard.class);
            i.module("player-name-header", PlayerNameHeader.class);
            i.module("chat-format", ChatFormat.class);
            i.module("we-session-renderer", WESessionRenderer.class);

            i.service(VisualScoreboardService.class);

            i.config("starlight-display");
            i.language("starlight-display", "zh_cn");
        });
    }


    static Set<PackageInitializer> initializers() {
        return Set.of(management(), console(), warps(), utilities(), display(), security());
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }
}
