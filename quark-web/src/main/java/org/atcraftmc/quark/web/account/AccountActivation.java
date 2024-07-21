package org.atcraftmc.quark.web.account;

import me.gb2022.apm.local.MappedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.quark.web.HttpService;
import org.atcraftmc.quark.web.SMTPService;
import org.atcraftmc.quark.web.http.ContentType;
import org.atcraftmc.quark.web.http.HttpHandlerContext;
import org.atcraftmc.quark.web.http.HttpRequest;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.assets.Asset;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

@CommandProvider({AccountCommand.class})
@QuarkModule(version = "1.0.2")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.PLUGIN_MESSAGE})
public final class AccountActivation extends PackageModule {
    private final PlayerFreezingManager freezingManager = new PlayerFreezingManager(this);
    private final Set<String> checkCache = new HashSet<>();

    @Inject("verify-result.html;true")
    private Asset verifyResultHtml;

    @Inject("verify.html;true")
    private Asset verifyHTML;

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        BukkitUtil.registerEventListener(this.freezingManager);
        HttpService.registerHandler(this);
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.testPlayer(p);
        }
    }

    @Override
    public void disable() {
        BukkitUtil.unregisterEventListener(this.freezingManager);

        for (Player p : Bukkit.getOnlinePlayers()) {
            this.unfreeze(p);
        }
    }

    //status
    @EventHandler
    public void onPlayerLeaved(PlayerQuitEvent event) {
        this.unfreeze(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(DelayedPlayerJoinEvent event) {
        this.testPlayer(event.getPlayer());
    }


    void testPlayer(Player p) {
        if (this.checkCache.contains(p.getName())) {
            return;
        }
        AccountStatus status = AccountManager.getStatus(p.getName());
        if (status.shouldAllowPlayerAction()) {
            this.unfreeze(p);
            p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            return;
        }
        this.freeze(p, status);
    }

    void freeze(Player player, AccountStatus status) {
        player.setGameMode(GameMode.SPECTATOR);

        if (status == AccountStatus.UNLINKED) {
            getLanguage().sendMessage(player, "link-hint");
        } else {
            getLanguage().sendMessage(player, "verify-hint");
        }
        this.checkCache.add(player.getName());
        this.freezingManager.freezePlayer(player.getName());

        String uuid = UUID.randomUUID().toString();

        Locale locale = Language.locale(player);
        TaskService.timerTask(uuid, 0, 20, () -> {
            if (status == AccountStatus.UNLINKED) {
                TextSender.fullTitle(player,
                        this.language.getMessageComponent(locale, "link-title"),
                        this.language.getMessageComponent(locale, "link-guide"),
                        0, 40, 0);
            } else {
                TextSender.fullTitle(player,
                        this.language.getMessageComponent(locale, "verify-title"),
                        this.language.getMessageComponent(locale, "verify-guide"),
                        0, 40, 0);
            }

            if (!player.isOnline()) {
                TaskService.cancelTask(uuid);
            }
        });
    }

    void unfreeze(Player player) {
        this.checkCache.remove(player.getName());
        this.freezingManager.unfreezePlayer(player.getName());
    }

    @HttpRequest("/account/verify")
    public void verifyLink(HttpHandlerContext context) {
        context.contentType(ContentType.HTML);

        try {
            boolean b = AccountManager.verifyMail(context.getParam("code"));

            String content = this.language.buildTemplate(Locale.CHINA, this.verifyResultHtml.asText());

            if (b) {
                content = content.replace("{title}", this.language.getMessage(Locale.SIMPLIFIED_CHINESE, "result-html-success-title"));
                content = content.replace("{content}", this.language.getMessage(Locale.SIMPLIFIED_CHINESE, "result-html-success-content"));
            } else {
                content = content.replace("{title}", this.language.getMessage(Locale.SIMPLIFIED_CHINESE, "result-html_failed-title"));
                content = content.replace("{content}", this.language.getMessage(Locale.SIMPLIFIED_CHINESE, "result-html-failed-content"));
            }

            context.setData(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            ExceptionUtil.log(getLogger(), e);
        }
    }

    public void sendVerifyMail(Player player, String mailBox, String code) {
        int safetyCode = new Random().nextInt(100000, 999999);

        SharedObjects.SHARED_THREAD_POOL.submit(() -> {
            String content = this.language.buildTemplate(Language.locale(player), this.verifyHTML.asText());

            content = content.replace("{player}", player.getName());
            content = content.replace("{link}", code);
            content = content.replace("{safety_code}", String.valueOf(safetyCode));
            String subject = this.language.getMessage(Language.locale(player), "mail-title");
            if (SMTPService.sendMailTo(mailBox, subject, content)) {
                this.language.sendMessage(player, "msg-send-complete", mailBox, safetyCode);
                return;
            }
            this.language.sendMessage(player, "msg-send-failed", mailBox);
        });
    }

    @PluginMessageHandler("ip:change")
    public void onIpFailure(MappedBroadcastEvent event) {
        String player = event.getProperty("player", String.class);
        AccountManager.setStatus(player, AccountStatus.UNVERIFIED);
        Player p = PlayerUtil.strictFindPlayer(event.getProperty("player", String.class));
        if (p == null) {
            return;
        }
        p.kickPlayer(this.language.getMessage(Language.locale(p), "kick_info"));
    }
}
