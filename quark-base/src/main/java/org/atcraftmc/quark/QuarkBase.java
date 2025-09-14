package org.atcraftmc.quark;

import org.atcraftmc.quark.automatic.AutoPluginReload;
import org.atcraftmc.quark.automatic.AutoSave;
import org.atcraftmc.quark.automatic.VMGarbageCleaner;
import org.atcraftmc.quark.chat.*;
import org.atcraftmc.quark.commands.*;
import org.atcraftmc.quark.display.*;
import org.atcraftmc.quark.security.*;
import org.atcraftmc.quark.utilities.*;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.packages.initializer.PackageBuilderInitializer;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.MultiPackageProvider;
import org.atcraftmc.starlight.framework.packages.provider.QuarkPackageProvider;

import java.util.Set;

@QuarkPackageProvider
public final class QuarkBase extends MultiPackageProvider {

    public static Set<PackageInitializer> initializers() {
        return Set.of(
                PackageBuilderInitializer.of("quark-commands", FeatureAvailability.BOTH, (i) -> {
                    i.module("self-message", SelfMessageCommand.class);
                    i.module("command-function", CommandFunction.class);
                    i.module("command-exec", CommandExec.class);

                    i.module("item-command", ItemCommand.class);
                    i.module("command-variables", CommandVariables.class);

                    i.language("quark-commands", "zh_cn");
                    i.language("quark-commands", "en_us");
                }),
                PackageBuilderInitializer.of("quark-utilities", FeatureAvailability.BOTH, (i) -> {
                    i.module("command-tab-fix", CommandTabFix.class);
                    i.module("player-ping-command", PlayerPingCommand.class);
                    i.module("tick-manager", TickManager.class);
                    i.module("block-update-locker", BlockUpdateLocker.class);
                    i.module("camera-movement", CameraMovement.class);
                    i.module("force-sprint", ForceSprint.class);
                    i.module("item-custom-name", ItemCustomName.class);
                    i.module("inventory-menu", InventoryMenu.class);
                    i.module("unexpected-kick-prevent", UnexpectedKickPrevent.class);

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

                    i.language("quark-security", "zh_cn");
                    i.config("quark-security");
                }), PackageBuilderInitializer.of("quark-display", FeatureAvailability.BOTH, (i) -> {
                    i.module("custom-motd", CustomMotd.class);
                    i.module("custom-kick-message", CustomKickMessage.class);
                    i.module("bossbar-announcement", BossbarAnnouncement.class);
                    i.module("join-quit-message", JoinQuitMessage.class);
                    i.module("chat-announce", ChatAnnounce.class);
                    i.module("hover-display", HoverDisplay.class);
                    i.module("nickname", Nickname.class);
                    //i.module("player-skin-customizer", PlayerSkinCustomizer.class);
                    i.module("drop-item-info", DropItemInfo.class);

                    i.language("quark-display", "zh_cn");
                    i.language("quark-display", "en_us");

                    i.config("quark-display");
                }), PackageBuilderInitializer.of("quark-chat", FeatureAvailability.BOTH, (i) -> {

                    i.module("chat-translator", ChatTranslator.class);
                    i.module("mail", Mail.class);

                    i.module("hitokoto", Hitokoto.class);
                    i.module("chatgpt", ChatGPT.class);
                    i.module("npc-chat", NPCChat.class);
                    i.module("qq-chat-sync", QQChatSync.class);
                    //i.module("display-setting", DisplaySetting.class);

                    i.language("quark-chat", "zh_cn");
                    i.language("quark-chat", "en_us");

                    i.config("quark-chat");
                })
        );
    }

    @Override
    public Set<PackageInitializer> createInitializers() {
        return initializers();
    }
}
