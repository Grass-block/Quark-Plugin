package org.tbstcraft.quark.util.platform;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.data.config.Queries;
import org.tbstcraft.quark.framework.event.BanMessageFetchEvent;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.util.text.TextBuilder;

import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public interface PlayerUtil {
    static List<String> getAllPlayerNames() {
        List<String> names = new ArrayList<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    static List<String> getAllOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        Collections.sort(names);
        return names;
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


    static int getPing(Player p) {
        try {
            Object craftPlayer = BukkitUtil.toCraftEntity(p);
            if (craftPlayer == null) {
                return 0;
            }
            return (int) craftPlayer.getClass().getMethod("getPing").invoke(craftPlayer);
        } catch (Exception e) {
            Quark.LOGGER.severe(e.getMessage());
            return 0;
        }
    }

    static void banPlayer(String target, BanList.Type type, String reason, Date expire, String source) {
        BanEntry entry = Bukkit.getBanList(type).addBan(target, reason, expire, source);
        if (type == BanList.Type.NAME) {
            Player p = PlayerUtil.strictFindPlayer(target);
            if (p == null) {
                return;
            }
            BanMessageFetchEvent e = new BanMessageFetchEvent(entry, BanList.Type.NAME, p.getLocale(), reason);
            BukkitUtil.callEventAsync(e);
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

    static String getLocale(Player p) {
        if (APIProfileTest.isPaperServer()) {
            return p.locale().toString().toLowerCase().replace("-", "_");
        } else {
            return p.getLocale();
        }
    }

    static void setPlayerTab(Player player, String header, String footer) {
        header = Queries.PLAYER_TEMPLATE_ENGINE.handle(player, header);
        footer = Queries.PLAYER_TEMPLATE_ENGINE.handle(player, footer);

        if (APIProfileTest.isPaperCompat()) {
            player.sendPlayerListHeader(TextBuilder.buildComponent(header));
            player.sendPlayerListFooter(TextBuilder.buildComponent(footer));
            return;
        }
        player.setPlayerListHeader(header);
        player.setPlayerListFooter(footer);
    }

    static long getPlayTime(Player player) {
        try {
            return System.currentTimeMillis() - player.getLastLogin();
        } catch (Throwable e) {
            return -1;
        }
    }

    static void teleport(Player p, Location loc) {
        if (APIProfileTest.isFoliaServer()) {
            p.teleportAsync(loc);
            return;
        }
        p.teleport(loc);
    }


    static void setViewDistance(Player p, int value) {
        if (value < 2 || value > 32) {
            throw new IllegalArgumentException("[2,32] required,but find " + value);
        }
        try {
            p.getClass().getMethod("setViewDistance", int.class).invoke(p, value);
        } catch (Throwable ignored) {
        }
    }

    static void setSendViewDistance(Player p, int value) {
        if (value < 2 || value > 32) {
            throw new IllegalArgumentException("[2,32] required,but find " + value);
        }
        try {
            p.getClass().getMethod("setSendViewDistance", int.class).invoke(p, value);
        } catch (Throwable ignored) {
        }
    }

    static Player strictFindPlayer(String name) {
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            return null;
        }
        if (!p.getName().equals(name)) {
            return null;
        }
        return p;
    }
}
