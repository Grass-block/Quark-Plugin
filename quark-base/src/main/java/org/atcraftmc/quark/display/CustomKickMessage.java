package org.atcraftmc.quark.display;

import me.gb2022.apm.local.MappedQueryEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.core.event.BanMessageFetchEvent;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.migration.MessageAccessor;

import java.util.Date;

@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.PLUGIN_MESSAGE})
@SLModule(version = "1.0.0")
public final class CustomKickMessage extends PackageModule {
    @SuppressWarnings("Convert2MethodRef")
    public static final MethodHandleO1<PlayerKickEvent, Component> SET_KICK_REASON = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> PlayerKickEvent.class.getMethod("reason", Component.class), (e, c) -> e.reason(c));
        ctx.dummy((e, c) -> e.setReason(ComponentSerializer.legacy(c)));
    });

    @SuppressWarnings("Convert2MethodRef")
    public static final MethodHandleO2<AsyncPlayerPreLoginEvent, AsyncPlayerPreLoginEvent.Result, Component> DISALLOW_LOGIN = MethodHandle.select(
            (ctx) -> {
                ctx.attempt(() -> AsyncPlayerPreLoginEvent.class.getMethod(
                        "disallow",
                        AsyncPlayerPreLoginEvent.Result.class,
                        Component.class
                ), (e, r, c) -> e.disallow(r, c));
                ctx.dummy((e, r, c) -> e.disallow(r, ComponentSerializer.legacy(c)));
            });


    @Inject
    private LanguageEntry language;

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().startsWith("\u0002")) {
            event.setReason(event.getReason().replaceFirst("\u0002", ""));
            return;
        }
        String msg = MessageAccessor.buildTemplate(
                this.language,
                LocaleService.locale(event.getPlayer()),
                Language.generateTemplate(this.getConfig(), "ui")
        );

        var c = TextBuilder.buildComponent(msg.replace("{reason}", event.getReason()));
        SET_KICK_REASON.invoke(event, c);
    }

    @PluginMessageHandler(PluginMessenger.FETCH_KICK_MESSAGE)
    public void onMessageFetch(MappedQueryEvent event) {
        String msg = event.getProperty("message", String.class);
        if (msg.startsWith("\u0002")) {
            event.setProperty("message", msg.replaceFirst("\u0002", ""));
            return;
        }

        var locale = Language.locale(event.getProperty("locale", String.class));
        var ui = MessageAccessor.buildTemplate(this.language, locale, Language.generateTemplate(this.getConfig(), "ui"));
        event.setProperty("message", PluginPlatform.global().globalFormatMessage(ui.replace("{reason}", msg)));
    }

    @EventHandler
    public void onBanMessageFetch(BanMessageFetchEvent event) {
        event.setMessage(buildBanUI(
                event.getType(),
                event.getMessage(),
                event.getExpiration(),
                event.getTarget(),
                event.getSource(),
                MinecraftLocale.minecraft(event.getLocale())
        ));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        var locale = MinecraftLocale.ZH_CN;
        String player = event.getName();
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        if (p.getPlayer() != null) {
            locale = LocaleService.locale(p.getPlayer());
        }

        String ui;

        if (Bukkit.getBanList(BanList.Type.NAME).isBanned(player)) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.NAME).getBanEntry(player);
            if (entry == null) {
                return;
            }
            ui = this.buildBanUI(BanList.Type.NAME, entry.getReason(), entry.getExpiration(), entry.getTarget(), entry.getSource(), locale);
        } else if (Bukkit.getBanList(BanList.Type.IP).isBanned(player)) {
            BanEntry entry = Bukkit.getBanList(BanList.Type.IP).getBanEntry(player);
            if (entry == null) {
                return;
            }
            ui = this.buildBanUI(BanList.Type.IP, entry.getReason(), entry.getExpiration(), entry.getTarget(), entry.getSource(), locale);
        } else {
            return;
        }

        DISALLOW_LOGIN.invoke(event, AsyncPlayerPreLoginEvent.Result.KICK_OTHER, TextBuilder.buildComponent(ui));
    }

    public String buildBanUI(BanList.Type type, String reason, Date expiration, String target, String source, MinecraftLocale locale) {
        String msg;
        if (type == BanList.Type.NAME) {
            msg = MessageAccessor.buildTemplate(
                    this.language,
                    locale,
                    Language.generateTemplate(this.getConfig(), "ban-ui", (s) -> s.replace("@type", "ban-name"))
            );
        } else {
            msg = MessageAccessor.buildTemplate(
                    this.language,
                    locale,
                    Language.generateTemplate(this.getConfig(), "ban-ui", (s) -> s.replace("@type", "ban-ip"))
            );
        }

        if (expiration == null) {
            msg = msg.replace("{expire}", "(forever)");
        } else {
            msg = msg.replace("{expire}", SharedObjects.DATE_FORMAT.format(expiration));
        }

        if (reason != null) {
            msg = msg.replace("{reason}", reason);
        } else {
            msg = msg.replace("{reason}", "(no reason)");
        }

        msg = msg.replace("{name}", target).replace("{source}", source);

        return msg;
    }
}
