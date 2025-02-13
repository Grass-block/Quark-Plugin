package org.atcraftmc.quark;

import org.atcraftmc.quark.automatic.AutoPluginReload;
import org.atcraftmc.quark.automatic.AutoSave;
import org.atcraftmc.quark.automatic.VMGarbageCleaner;
import org.atcraftmc.quark.chat.*;
import org.atcraftmc.quark.commands.*;
import org.atcraftmc.quark.display.*;
import org.atcraftmc.quark.management.*;
import org.atcraftmc.quark.security.*;
import org.atcraftmc.quark.utilities.*;
import org.atcraftmc.quark.utilities.viewdistance.DynamicViewDistance;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.packages.initializer.PackageBuilderInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.MultiPackageProvider;
import org.tbstcraft.quark.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkBase extends MultiPackageProvider {

    public static Set<PackageInitializer> initializers() {
        return Set.of(
                PackageBuilderInitializer.of("quark-commands", FeatureAvailability.BOTH, (i) -> {
                    i.module("self-message", SelfMessageCommand.class);
                    i.module("command-function", CommandFunction.class);
                    i.module("console-command", ConsoleCommand.class);
                    i.module("command-exec", CommandExec.class);
                    i.module("entity-motion", EntityMotion.class);
                    i.module("position-align", PositionAlign.class);
                    i.module("item-command", ItemCommand.class);
                    i.module("command-variables", CommandVariables.class);

                    i.language("quark-commands", "zh_cn");
                    i.language("quark-commands", "en_us");
                }),


                PackageBuilderInitializer.of("quark-utilities", FeatureAvailability.BOTH, (i) -> {
                    i.module("command-tab-fix", CommandTabFix.class);
                    i.module("calculator", Calculator.class);
                    i.module("player-ping-command", PlayerPingCommand.class);
                    i.module("custom-log-format", CustomLogFormat.class);
                    i.module("dynamic-view-distance", DynamicViewDistance.class);
                    i.module("tick-manager", TickManager.class);
                    i.module("block-update-locker", BlockUpdateLocker.class);
                    i.module("player-position-lock", PlayerPositionLock.class);
                    i.module("camera-movement", CameraMovement.class);
                    i.module("force-sprint", ForceSprint.class);
                    i.module("worldedit-commands", WorldEditCommands.class);
                    i.module("surrounding-refresh", SurroundingRefresh.class);
                    i.module("item-custom-name", ItemCustomName.class);

                    i.language("quark-utilities", "zh_cn");
                    i.language("quark-utilities", "en_us");
                    i.config("quark-utilities");
                }), PackageBuilderInitializer.of("quark-automatic", FeatureAvailability.BOTH, (i) -> {
                    i.module("vm-garbage-cleaner", VMGarbageCleaner.class);
                    i.module("auto-plugin-reload", AutoPluginReload.class);
                    i.module("auto-save", AutoSave.class);

                    i.language("quark-automatic", "zh_cn");
                    i.config("quark-automatic");
                }), PackageBuilderInitializer.of("quark-security", FeatureAvailability.BOTH, (i) -> {
                    i.module("explosion-defender", ExplosionDefender.class);
                    i.module("permission-manager", PermissionManager.class);
                    i.module("we-session-size-limit", WESessionSizeLimit.class);
                    i.module("item-defender", ItemDefender.class);
                    i.module("protection-area", ProtectionArea.class);
                    i.module("advanced-permission-control", AdvancedPermissionControl.class);
                    i.module("ip-defender", IPDefender.class);

                    i.service(WESessionTrackService.class);

                    i.language("quark-security", "zh_cn");
                    i.config("quark-security");
                }), PackageBuilderInitializer.of("quark-display", FeatureAvailability.BOTH, (i) -> {
                    i.module("custom-motd", CustomMotd.class);
                    i.module("custom-ban-message", CustomBanMessage.class);
                    i.module("custom-kick-message", CustomKickMessage.class);
                    i.module("bossbar-announcement", BossbarAnnouncement.class);
                    i.module("join-quit-message", JoinQuitMessage.class);
                    i.module("tab-menu", TabMenu.class);
                    i.module("chat-format", ChatFormat.class);
                    i.module("player-name-header", PlayerNameHeader.class);
                    i.module("we-session-renderer", WESessionRenderer.class);
                    i.module("welcome-message", WelcomeMessage.class);
                    i.module("custom-scoreboard", CustomScoreboard.class);
                    i.module("chat-announce", ChatAnnounce.class);
                    i.module("hover-display", HoverDisplay.class);
                    i.module("nickname", Nickname.class);
                    i.module("custom-death-message", CustomDeathMessage.class);
                    i.module("afk", AFK.class);
                    //i.module("player-skin-customizer", PlayerSkinCustomizer.class);
                    i.module("action-bar-hud", ActionBarHUD.class);
                    i.module("drop-item-info", DropItemInfo.class);

                    i.language("quark-display", "zh_cn");
                    i.language("quark-display", "en_us");

                    i.service(ChatForwardingService.class);

                    i.config("quark-display");
                }), PackageBuilderInitializer.of("quark-chat", FeatureAvailability.BOTH, (i) -> {
                    i.module("chat-filter", ChatFilter.class);
                    i.module("chat-mute", ChatMute.class);
                    i.module("chat-translator", ChatTranslator.class);
                    i.module("chat-component", ChatComponent.class);
                    i.module("chat-at", ChatAt.class);
                    i.module("mail", Mail.class);
                    i.module("chat-report", ChatReport.class);
                    i.module("hitokoto", Hitokoto.class);
                    i.module("chatgpt", ChatGPT.class);
                    i.module("npc-chat", NPCChat.class);
                    i.module("qq-chat-sync", QQChatSync.class);
                    //i.module("display-setting", DisplaySetting.class);

                    i.language("quark-chat", "zh_cn");
                    i.language("quark-chat", "en_us");

                    i.config("quark-chat");
                }), PackageBuilderInitializer.of("quark-management", FeatureAvailability.BOTH, (i) -> {
                    i.module("advanced-plugin-command", AdvancedPluginCommand.class);
                    i.module("kick-on-reload", KickOnReload.class);
                    i.module("maintenance", Maintenance.class);
                    i.module("advanced-ban-command", AdvancedBan.class);
                    i.module("stop-confirm", StopConfirm.class);
                    i.module("server-info", ServerInfo.class);

                    i.language("quark-management", "zh_cn");
                    i.language("quark-management", "en_us");
                    i.config("quark-management");
                }));
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }
}
