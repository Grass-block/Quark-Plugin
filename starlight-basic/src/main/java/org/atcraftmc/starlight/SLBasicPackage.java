package org.atcraftmc.starlight;

import me.gb2022.modular.FeatureAvailability;
import org.atcraftmc.starlight.chat.ChatAt;
import org.atcraftmc.starlight.chat.ChatComponent;
import org.atcraftmc.starlight.commands.*;
import org.atcraftmc.starlight.console.*;
import org.atcraftmc.starlight.display.*;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.management.*;
import org.atcraftmc.starlight.security.*;
import org.atcraftmc.starlight.sideload.InventoryMenu;
import org.atcraftmc.starlight.sideload.RecipeLoader;
import org.atcraftmc.starlight.utilities.*;
import org.atcraftmc.starlight.warp.BackToDeath;
import org.atcraftmc.starlight.warp.RTP;
import org.atcraftmc.starlight.warp.TPA;
import org.atcraftmc.starlight.warp.Waypoints;

import java.util.Set;

public final class SLBasicPackage extends MultiPackageProvider {

    static SLPackageInitializer management() {
        return SLPackageBuilder.of("starlight-management", FeatureAvailability.BOTH, (i) -> {
            i.module(Ban.class);
            i.module(ChatFilter.class);
            i.module(ChatReport.class);
            i.module(Maintenance.class);
            i.module(Mute.class); //todo [DFU] import mute status
            i.module(TPSBar.class);
            i.module(ServerInfo.class);
            i.module(KickOnReload.class);
            i.module(PluginManagerCommand.class);

            i.config("starlight-management");
            i.language("starlight-management", "zh_cn");
        });
    }

    static SLPackageInitializer console() {
        return SLPackageBuilder.of("starlight-console", FeatureAvailability.BOTH, (i) -> {
            i.module(ClearConsole.class);
            i.module(ConsoleExecute.class);
            i.module(CustomLogFormat.class);
            i.module(LogColorPatch.class);
            i.module(StopConfirm.class);

            i.language("starlight-console", "zh_cn");
        });
    }

    static SLPackageInitializer warps() {
        return SLPackageBuilder.of("starlight-warps", FeatureAvailability.BOTH, (i) -> {
            i.module(Waypoints.class); //todo [DFU] import waypoints
            i.module(RTP.class);
            i.module(TPA.class);
            i.module(BackToDeath.class);

            i.config("starlight-warps");
            i.language("starlight-warps", "zh_cn");
        });
    }

    static SLPackageInitializer utilities() {
        return SLPackageBuilder.of("starlight-utilities", FeatureAvailability.BOTH, (i) -> {
            i.module(BlockUpdateLocker.class);
            i.module(Calculator.class);
            i.module(CameraMovement.class);
            i.module(DynamicViewDistance.class);
            i.module(Hitokoto.class);
            i.module(PlayerPingCommand.class);
            i.module(PositionLock.class);
            i.module(PositionAlign.class);
            i.module(SurroundingRefresh.class);
            i.module(TickManager.class);
            i.module(FreeCam.class);
            i.module(FlyCommand.class);

            i.config("starlight-utilities");
            i.language("starlight-utilities", "zh_cn");
        });
    }

    //command extension and triggers
    static SLPackageInitializer commands() {
        return SLPackageBuilder.of("starlight-commands", FeatureAvailability.BOTH, (i) -> {
            i.module(CommandTabFix.class);
            i.module(Execute.class);
            i.module(EntityMotion.class);
            i.module(ItemBinding.class);
            i.module(WorldEditCommands.class);
            i.module(AnimateBlockCommand.class);

            i.language("starlight-commands", "zh_cn");
        });
    }

    static SLPackageInitializer security() {
        return SLPackageBuilder.of("starlight-security", FeatureAvailability.BOTH, (i) -> {
            i.module(AdvancedPermissionControl.class);
            i.module(ExplosionDefender.class); //todo [DFU] import whitelist region
            i.module(IMGRegulationSync.class);
            i.module(IPDefender.class); //todo [DFU] miss
            i.module(PermissionManager.class); //todo [DFU] import permission data
            i.module(WorldEditOperationDefender.class);

            i.service(WESessionTrackService.class);

            i.config("starlight-security");
            i.language("starlight-security", "zh_cn");
        });
    }

    static SLPackageInitializer chat() {
        return SLPackageBuilder.of("starlight-chat", FeatureAvailability.BOTH, (i) -> {
            i.module(ChatAt.class);
            i.module(ChatComponent.class);

            i.config("starlight-chat");
            i.language("starlight-chat", "zh_cn");
        });
    }

    static SLPackageInitializer display() {
        return SLPackageBuilder.of("starlight-display", FeatureAvailability.BOTH, (i) -> {
            i.module(ActionBarHUD.class);
            i.module(AFK.class);
            i.module(ChatFormat.class);
            i.module(CustomDeathMessage.class);
            i.module(CustomMotd.class);
            i.module(CustomScoreboard.class);
            i.module(DropItemInfo.class);
            //i.module("hover-display", HoverDisplay.class); //todo [DFU] refine + import hover data
            i.module(PlayerNameHeader.class); //todo [DFU] import header
            i.module(TabMenu.class);
            i.module(WelcomeMessage.class);
            i.module(WESessionRenderer.class);
            i.module(CustomKickMessage.class);

            i.service(VisualScoreboardService.class);
            i.service(PlayerWelcomeService.class); //todo [DFU] import status-> first-join-detection

            i.config("starlight-display");
            i.language("starlight-display", "zh_cn");
        });
    }

    static SLPackageInitializer sideload() {
        return SLPackageBuilder.of("starlight-sideload", FeatureAvailability.BOTH, (i) -> {
            i.module(RecipeLoader.class);
            i.module(InventoryMenu.class);
        });
    }

    static Set<SLPackageInitializer> initializers() {
        return Set.of(management(), console(), warps(), utilities(), display(), security(), sideload(), commands(), chat());
    }

    @Override
    public Set<SLPackageInitializer> createInitializers() {
        return initializers();
    }
}
