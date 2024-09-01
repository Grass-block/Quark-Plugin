package org.tbstcraft.quark.foundation.platform;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO1;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.text.ComponentSerializer;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.event.BanMessageFetchEvent;
import org.tbstcraft.quark.framework.module.ModuleManager;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("Convert2MethodRef")
public interface Players {
    MethodHandleRO0<Player, Long> LAST_LOGIN = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getLastLogin"), (p) -> p.getLastLogin());
        ctx.dummy((p) -> System.currentTimeMillis());
    });
    MethodHandleRO0<Player, Integer> PING = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("getPing"), (p) -> p.getPing());
        ctx.dummy((p) -> 0);
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
    MethodHandleO1<Entity, Location> TELEPORT = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Entity.class.getMethod("teleportAsync", Location.class), (p, l) -> p.teleportAsync(l));
        ctx.attempt(() -> Entity.class.getMethod("teleport", Location.class), Entity::teleport);
    });
    MethodHandleO1<Player, Component> SEND_MESSAGE = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Player.class.getMethod("sendMessage", Component.class), (p, c) -> p.sendMessage(c));
        ctx.attempt(
                () -> Player.Spigot.class.getMethod("sendMessage", BaseComponent.class),
                (p, c) -> p.spigot().sendMessage(ComponentSerializer.bungee(c))
        );
        ctx.attempt(
                () -> Player.class.getMethod("sendMessage", String.class),
                (p, c) -> p.sendMessage(ComponentSerializer.legacy(c))
        );
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

    static void sendMessage(Player p, Component msg) {
        SEND_MESSAGE.invoke(p, msg);
    }

    static void teleport(Entity p, Location loc) {
        TELEPORT.invoke(p, loc);
    }

    static void setPlayerTab(Player p, Component header, Component footer) {
        SET_TAB.invoke(p, header, footer);
    }


    //----[Utilities]----
    static void banPlayer(String target, BanList.Type type, String reason, Date expire, String source) {
        BanEntry<?> entry = Bukkit.getBanList(type).addBan(target, reason, expire, source);
        if (type == BanList.Type.NAME) {
            Player p = Bukkit.getPlayerExact(target);
            if (p == null) {
                return;
            }
            BanMessageFetchEvent e = new BanMessageFetchEvent(entry, BanList.Type.NAME, p.getLocale(), reason);
            BukkitUtil.callEvent(e);
            if (ModuleManager.isEnabled("quark-display:custom-kick-message")) {
                p.kickPlayer("\u0002" + e.getMessage());
            } else {
                p.kickPlayer(e.getMessage());
            }
        }
    }

    static void show3DBox(Player player, Location point0, Location point1) {
        double x0 = Math.min(point0.getX(), point1.getX());
        double y0 = Math.min(point0.getY(), point1.getY());
        double z0 = Math.min(point0.getZ(), point1.getZ());
        double x1 = Math.max(point0.getX(), point1.getX()) + 1;
        double y1 = Math.max(point0.getY(), point1.getY()) + 1;
        double z1 = Math.max(point0.getZ(), point1.getZ()) + 1;

        if ((x1 - x0) / 0.25 > 512) {
            return;
        }
        if ((y1 - y0) / 0.25 > 512) {
            return;
        }
        if ((z1 - z0) / 0.25 > 512) {
            return;
        }

        for (double i = x0; i <= x1; i += 0.25) {
            player.spawnParticle(Particle.END_ROD, i, y0, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, i, y1, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, i, y0, z1, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, i, y1, z1, 0, 0, 0, 0, 1);
        }

        for (double i = y0; i <= y1; i += 0.25) {
            player.spawnParticle(Particle.END_ROD, x0, i, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x1, i, z0, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x0, i, z1, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x1, i, z1, 0, 0, 0, 0, 1);
        }

        for (double i = z0; i <= z1; i += 0.25) {
            player.spawnParticle(Particle.END_ROD, x0, y0, i, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x1, y0, i, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x0, y1, i, 0, 0, 0, 0, 1);
            player.spawnParticle(Particle.END_ROD, x1, y1, i, 0, 0, 0, 0, 1);
        }
    }

    static void sendActionBarTitle(Player player, String message) {
        if (APIProfileTest.isPaperCompat()) {
            player.sendActionBar(Component.text(message));
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
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
