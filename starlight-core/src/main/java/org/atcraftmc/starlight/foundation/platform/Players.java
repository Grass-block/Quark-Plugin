package org.atcraftmc.starlight.foundation.platform;

import me.gb2022.commons.TriState;
import me.gb2022.commons.reflect.method.*;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.event.BanMessageFetchEvent;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.framework.module.ModuleManager;
import org.atcraftmc.starlight.internal.PlatformPatcher;
import org.atcraftmc.starlight.internal.platform.SpigotReflection;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@SuppressWarnings("Convert2MethodRef")
public interface Players {
    MethodHandleRO0<Player, Long> LAST_LOGIN = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getLastLogin"), (p) -> p.getLastLogin());
        ctx.dummy((p) -> PlatformPatcher.instance().map((c) -> c.lastLogin().get(p)).orElse(System.currentTimeMillis()));
    });
    MethodHandleRO0<Player, Integer> PING = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getPing"), (p) -> p.getPing());
        ctx.dummy((p) -> SpigotReflection.instance().ping(p));
    });
    MethodHandleO1<Player, Collection<String>> ADD_CHAT_TAB = MethodHandle.select((ctx) -> {
        ctx.attempt(
                () -> Player.class.getMethod("addCustomChatCompletions", Collection.class),
                (p, a) -> p.addCustomChatCompletions(a)
        );
        ctx.attempt(
                () -> Player.class.getMethod("addAdditionalChatCompletions", Collection.class),
                (p, a) -> p.removeCustomChatCompletions(a)
        );
        ctx.dummy((p, a1) -> {
        });
    });
    MethodHandleO1<Player, Collection<String>> REMOVE_CHAT_TAB = MethodHandle.select((ctx) -> {
        ctx.attempt(
                () -> Player.class.getMethod("removeCustomChatCompletions", Collection.class),
                (p, c) -> p.removeCustomChatCompletions(c)
        );
        ctx.attempt(
                () -> Player.class.getMethod("removeAdditionalChatCompletions", Collection.class),
                (p, c) -> p.removeAdditionalChatCompletions(c)
        );
        ctx.dummy((p, a1) -> {
        });
    });
    MethodHandleRO1<Entity, CompletableFuture<?>, Location> TELEPORT = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Entity.class.getMethod("teleportAsync", Location.class), (p, l) -> p.teleportAsync(l));
        ctx.attempt(() -> Entity.class.getMethod("teleport", Location.class), (p, l) -> {
            p.teleport(l);
            var f = new CompletableFuture<>();
            f.complete(true);
            return f;
        });
    });
    MethodHandleO2<Player, Component, Component> SET_TAB = MethodHandle.select((ctx) -> {
        ctx.attempt(
                () -> Player.class.getMethod("sendPlayerListHeader", Component.class),
                (p, c1, c2) -> p.sendPlayerListHeaderAndFooter(c1, c2)
        );
        ctx.attempt(
                () -> Player.class.getMethod("setPlayerListHeaderFooter", BaseComponent.class, BaseComponent.class),
                (p, c1, c2) -> {
                    var cc1 = ComponentSerializer.bungee(c1);
                    var cc2 = ComponentSerializer.bungee(c2);
                    p.setPlayerListHeaderFooter(cc1, cc2);
                }
        );
        ctx.attempt(() -> Player.class.getMethod("setPlayerListHeader", String.class), (p, c1, c2) -> {
            p.setPlayerListHeader(ComponentSerializer.legacy(c1));
            p.setPlayerListFooter(ComponentSerializer.legacy(c2));
        });
    });
    MethodHandleO1<Player, Component> KICK = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("kick", Component.class), (p, c) -> p.kick(c));
        ctx.dummy((p, c) -> p.kickPlayer(ComponentSerializer.legacy(c)));
    });

    //----[API]----
    static int getPing(Player p) {
        return PING.invoke(p);
    }

    static long getLastLogin(Player player) {
        return LAST_LOGIN.invoke(player);
    }

    static void addChatTabOption(Player player, String... opt) {
        ADD_CHAT_TAB.invoke(player, Set.of(opt));
    }

    static void removeChatTabOption(Player player, String... opt) {
        REMOVE_CHAT_TAB.invoke(player, Set.of(opt));
    }

    static CompletableFuture<?> teleport(Entity p, Location loc) {
        return TELEPORT.invoke(p, loc);
    }

    static void setPlayerTab(Player p, Component header, Component footer) {
        SET_TAB.invoke(p, header, footer);
    }


    //----[Utilities]----
    static void banPlayer(String target, BanList.Type type, String reason, Date expire, String source) {
        if (type == BanList.Type.NAME) {
            var player = Bukkit.getPlayerExact(target);
            if (player == null) {
                return;
            }
            BanMessageFetchEvent e = new BanMessageFetchEvent(
                    BanList.Type.NAME,
                    reason,
                    expire,
                    source,
                    player.getName(),
                    LocaleService.saveGetMCPlayerLocale(player)
            );
            BukkitUtil.callEvent(e, (ev -> TaskService.entity(player).run(() -> {
                if (ModuleManager.getInstance().getStatus("quark-display:custom-kick-message") != TriState.TRUE) {
                    KICK.invoke(player, TextBuilder.buildComponent("\u0002" + ev.getMessage()));
                } else {
                    KICK.invoke(player, TextBuilder.buildComponent(ev.getMessage()));
                }
                Bukkit.getBanList(type).addBan(target, reason, expire, source);
            })));
            return;
        }
        Bukkit.getBanList(type).addBan(target, reason, expire, source);
    }

    static void renderBox(Player player, Location point0, Location point1, double density) {
        double x0 = Math.min(point0.getX(), point1.getX());
        double y0 = Math.min(point0.getY(), point1.getY());
        double z0 = Math.min(point0.getZ(), point1.getZ());
        double x1 = Math.max(point0.getX(), point1.getX()) + 1;
        double y1 = Math.max(point0.getY(), point1.getY()) + 1;
        double z1 = Math.max(point0.getZ(), point1.getZ()) + 1;

        if ((x1 - x0) / density > 512) {
            return;
        }
        if ((y1 - y0) / density > 512) {
            return;
        }
        if ((z1 - z0) / density > 512) {
            return;
        }

        for (double i = x0; i <= x1; i += density) {
            player.spawnParticle(Particle.FLAME, i, y0, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, i, y1, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, i, y0, z1, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, i, y1, z1, 0, 0, 0, 0, 1);
        }

        for (double i = y0; i <= y1; i += density) {
            player.spawnParticle(Particle.FLAME, x0, i, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, x1, i, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, x0, i, z1, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.FLAME, x1, i, z1, 0, 0, 0, 0, 1);
        }

        for (double i = z0; i <= z1; i += density) {
            player.spawnParticle(Particle.FLAME, x0, y0, i, 0, 0, 0, 0, 0);
            player.spawnParticle(Particle.FLAME, x1, y0, i, 0, 0, 0, 0, 0);
            player.spawnParticle(Particle.FLAME, x0, y1, i, 0, 0, 0, 0, 0);
            player.spawnParticle(Particle.FLAME, x1, y1, i, 0, 0, 0, 0, 0);
        }
    }

    static void show3DBox(Player player, Location point0, Location point1) {
        renderBox(player, point0, point1, 0.25);
    }

    static void setPlayerTab(Player player, String header, String footer) {
        var h = TextBuilder.buildComponent(header);
        var f = TextBuilder.buildComponent(footer);

        setPlayerTab(player, h, f);
    }

    static long getPlayTime(Player player) {
        return System.currentTimeMillis() - getLastLogin(player);
    }

    //----[Query]----
    static List<String> getAllPlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList();
    }

    static List<String> getAllOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }

    static Set<Player> getOnlinePlayers(Predicate<Player> filter) {
        Set<Player> players = new HashSet<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!filter.test(p)) {
                continue;
            }
            players.add(p);
        }
        return players;
    }
}
