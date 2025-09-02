package org.atcraftmc.quark.web.account;

import me.gb2022.apm.local.MappedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.ComponentBlock;
import org.atcraftmc.quark.web.SMTPService;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.data.assets.Asset;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.TemplateExpansion;

import java.util.*;

@CommandProvider({AccountCommand.class})
@SLModule(version = "1.0.2", defaultEnable = false)
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.PLUGIN_MESSAGE})
public final class AccountActivation extends PackageModule {
    private final Map<String, OriginalPlayerStatus> playerStatus = new HashMap<>();
    private final PlayerFreezingManager freezingManager = new PlayerFreezingManager(this);
    private final Set<String> checkCache = new HashSet<>();

    @Inject("verify.html;true")
    private Asset verifyHTML;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(Starlight.instance(), "quark_pl:player");

        BukkitUtil.registerEventListener(this.freezingManager);
        Bukkit.getOnlinePlayers().forEach(this::testPlayer);

        TaskService.global().timer("mail:title:tick", 20, 20, () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (this.freezingManager.getWhiteList().contains(player.getName())) {
                    continue;
                }

                var status = AccountManager.getStatus(player.getName());
                var lang = this.getLanguage();
                var locale = LocaleService.locale(player);

                ComponentBlock title;
                ComponentBlock subtitle;

                if (status == AccountStatus.UNLINKED) {
                    title = lang.item("link-title").component(locale);
                    subtitle = lang.item("link-guide").component(locale);
                } else {
                    title = lang.item("verify-title").component(locale);
                    subtitle = lang.item("verify-guide").component(locale);
                }

                TextSender.sendTitle(player, title, subtitle, 0, 40, 1);
            }
        });
    }

    @Override
    public void disable() {
        BukkitUtil.unregisterEventListener(this.freezingManager);
        Bukkit.getOnlinePlayers().forEach(this::unfreeze);

        TaskService.global().cancel("mail:title:tick");
    }

    //status
    @EventHandler
    public void onPlayerLeaved(PlayerQuitEvent event) {
        this.unfreeze(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.testPlayer(event.getPlayer());
    }

    void testPlayer(Player p) {
        if (this.checkCache.contains(p.getName())) {
            return;
        }
        AccountStatus status = AccountManager.getStatus(p.getName());
        if (status.shouldAllowPlayerAction()) {
            this.unfreeze(p);
            return;
        }
        this.freeze(p, status);
    }

    void freeze(Player player, AccountStatus status) {
        player.sendPluginMessage(Starlight.instance(), "quark_pl:player", new byte[]{0x01});

        if (status == AccountStatus.UNLINKED) {
            MessageAccessor.send(this.getLanguage(), player, "link-hint");
        } else {
            MessageAccessor.send(this.getLanguage(), player, "verify-hint");
        }

        this.playerStatus.put(player.getName(), OriginalPlayerStatus.record(player));
        this.checkCache.add(player.getName());
        this.freezingManager.freezePlayer(player.getName());
    }

    void unfreeze(Player player) {
        player.sendPluginMessage(Starlight.instance(), "quark_pl:player", new byte[]{0x02});
        this.checkCache.remove(player.getName());
        this.freezingManager.unfreezePlayer(player.getName());
        Optional.ofNullable(this.playerStatus.remove(player.getName())).ifPresent((s) -> s.revoke(player));
    }

    public void sendVerifyMail(Player player, String mailBox, String code) {
        var safetyCode = new Random().nextInt(100000, 999999);
        MessageAccessor.send(this.language, player, "msg-send-hint");

        TaskService.async().run(() -> {
            var template = MessageAccessor.buildTemplate(this.language, LocaleService.locale(player), this.verifyHTML.asText());
            var subject = MessageAccessor.getMessage(this.language, LocaleService.locale(player), "mail-title");
            var content = TemplateExpansion.build((b) -> {
                b.replacement("player");
                b.replacement("link");
                b.replacement("safety_code");
            }).expand(template, player.getName(), code, safetyCode);

            if (SMTPService.sendMailTo(mailBox, subject, content)) {
                MessageAccessor.send(this.language, player, "msg-send-complete", mailBox, safetyCode);
                return;
            }
            MessageAccessor.send(this.language, player, "msg-send-failed", mailBox);
        });
    }

    @PluginMessageHandler("ip:change")
    public void onIpFailure(MappedBroadcastEvent event) {
        var player = event.getProperty("player", String.class);
        var name = event.getProperty("player", String.class);
        var p = Bukkit.getPlayerExact(name);

        AccountManager.setStatus(player, AccountStatus.UNVERIFIED);

        if (p == null) {
            return;
        }

        p.kickPlayer(MessageAccessor.getMessage(this.language, LocaleService.locale(p), "kick_info"));
    }


    record OriginalPlayerStatus(GameMode gameMode, float walkSpeed, float flySpeed) {
        private static OriginalPlayerStatus record(Player player) {
            var mode = player.getGameMode();
            var speed = player.getWalkSpeed();
            var flySpeed = player.getFlySpeed();

            player.setGameMode(GameMode.SPECTATOR);
            player.setWalkSpeed(0);
            player.setFlySpeed(0);

            return new OriginalPlayerStatus(mode, speed, flySpeed);
        }

        private void revoke(Player player) {
            player.setGameMode(this.gameMode);
            player.setWalkSpeed(this.walkSpeed);
            player.setFlySpeed(this.flySpeed);
        }
    }
}
