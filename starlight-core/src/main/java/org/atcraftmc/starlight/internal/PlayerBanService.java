package org.atcraftmc.starlight.internal;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.injection.ServiceInject;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.api.event.BanMessageFetchEvent;
import org.atcraftmc.starlight.api.event.KickMessageFetchEvent;
import org.atcraftmc.starlight.api.event.PlayerExtraBanCheckEvent;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@ApplicationService(id = "player-ban-service")
@SuppressWarnings("Convert2MethodRef")
public interface PlayerBanService extends SLService {
    LoginHandler EVENT_LISTENER = new LoginHandler();
    String MODIFIED_REASON_HEADER = "\u0002";

    MethodHandleO2<AsyncPlayerPreLoginEvent, AsyncPlayerPreLoginEvent.Result, Component> DISALLOW_LOGIN = MethodHandle.select((ctx) -> {
        ctx.attempt(
                () -> AsyncPlayerPreLoginEvent.class.getMethod("disallow", AsyncPlayerPreLoginEvent.Result.class, Component.class),
                (e, r, c) -> e.disallow(r, c)
        );
        ctx.dummy((e, r, c) -> e.disallow(r, ComponentSerializer.legacy(c)));
    });
    MethodHandleO1<PlayerKickEvent, Component> SET_KICK_REASON = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> PlayerKickEvent.class.getMethod("reason", Component.class), (e, c) -> {
            e.reason(c);
            e.setReason(ComponentSerializer.legacy(c));
        });
        ctx.dummy((e, c) -> e.setReason(ComponentSerializer.legacy(c)));
    });

    @ServiceInject
    static void start() {
        BukkitUtil.registerEventListener(EVENT_LISTENER);
    }

    @ServiceInject
    static void stop() {
        BukkitUtil.unregisterEventListener(EVENT_LISTENER);
    }

    static void banPlayer(BanList.Type type, String target, String source, String reason, Date expire) {
        if (type == BanList.Type.NAME) {
            var player = Bukkit.getPlayerExact(target);
            if (player == null) {
                return;
            }
            TaskService.async().run(() -> {
                var m = getBanMessage(LocaleService.locale(player), target, source, reason, expire);
                TaskService.entity(player).run(() -> Players.KICK.invoke(player, m));
            });
        }

        Bukkit.getBanList(type).addBan(target, reason, expire, source);
    }

    static Component getBanMessage(MinecraftLocale locale, String target, String source, String reason, Date expire) {
        var e2 = new BanMessageFetchEvent(BanList.Type.NAME, locale, target, source, reason, expire);
        var it = Starlight.instance().language().item("starlight-core.ban-service.default-line");
        e2.setResultMessage(it.message(locale, source, reason, expire));
        var ui = BukkitUtil.callEventUnsafe(e2).getResultMessage();

        return TextBuilder.buildComponent(MODIFIED_REASON_HEADER + ui);
    }

    final class LoginHandler implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void onKick(PlayerKickEvent event) throws ExecutionException, InterruptedException {
            if (event.getReason().startsWith(MODIFIED_REASON_HEADER)) {
                event.setReason(event.getReason().replaceFirst(MODIFIED_REASON_HEADER, ""));
                return;
            }

            var l = LocaleService.locale(event.getPlayer());
            var r = event.getReason();
            var p = event.getPlayer();

            var e = new KickMessageFetchEvent(p, r, l);
            var it = Starlight.instance().language().item("starlight-core.ban-service.default-kick-line");
            e.setResultMessage(it.message(l, r));
            BukkitUtil.callEvent(e).get();

            SET_KICK_REASON.invoke(event, TextBuilder.buildComponent(e.getResultMessage()));
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onConnect(AsyncPlayerPreLoginEvent event) {
            var playerId = event.getName();
            var p = Bukkit.getOfflinePlayer(playerId);
            var locale = LocaleService.locale(p);

            if (checkBanList(event, BanList.Type.NAME, locale, playerId)) {
                return;
            }
            if (checkBanList(event, BanList.Type.IP, locale, event.getAddress().toString())) {
                return;
            }

            var e = new PlayerExtraBanCheckEvent(event.getUniqueId());
            if (!BukkitUtil.callEventUnsafe(e).isBanned()) {
                return;
            }

            DISALLOW_LOGIN.invoke(
                    event,
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    getBanMessage(locale, p.getName(), e.getSource(), e.getReason(), e.getExpires())
            );
        }

        private boolean checkBanList(AsyncPlayerPreLoginEvent event, BanList.Type type, MinecraftLocale locale, String query) {
            if (!Bukkit.getBanList(type).isBanned(query)) {
                return false;
            }

            var entry = Bukkit.getBanList(type).getBanEntry(query);

            if (entry == null) {
                return false;
            }

            DISALLOW_LOGIN.invoke(
                    event,
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    getBanMessage(locale, query, entry.getSource(), entry.getReason(), entry.getExpiration())
            );

            return true;
        }
    }
}
