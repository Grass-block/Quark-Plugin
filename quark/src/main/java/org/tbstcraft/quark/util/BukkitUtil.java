package org.tbstcraft.quark.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.tbstcraft.quark.Quark;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface BukkitUtil {
    AtomicInteger TPS = new AtomicInteger(20);

    static String formatChatComponent(String s) {
        String s2 = ChatColor.GREEN + String.valueOf(TPS.intValue());
        if (TPS.get() < 17) {
            s2 = ChatColor.YELLOW + String.valueOf(TPS.intValue());
        }
        if (TPS.get() < 10) {
            s2 = ChatColor.RED + String.valueOf(TPS.intValue());
        }
        return s
                //color
                .replace("{black}", ChatColor.BLACK.toString())
                .replace("{dark_blue}", ChatColor.DARK_BLUE.toString())
                .replace("{dark_green}", ChatColor.DARK_GREEN.toString())
                .replace("{dark_aqua}", ChatColor.DARK_AQUA.toString())
                .replace("{dark_red}", ChatColor.DARK_RED.toString())
                .replace("{dark_purple}", ChatColor.DARK_PURPLE.toString())
                .replace("{gold}", ChatColor.GOLD.toString())
                .replace("{gray}", ChatColor.GRAY.toString())
                .replace("{dark_gray}", ChatColor.DARK_GRAY.toString())
                .replace("{blue}", ChatColor.BLUE.toString())
                .replace("{green}", ChatColor.GREEN.toString())
                .replace("{aqua}", ChatColor.AQUA.toString())
                .replace("{red}", ChatColor.RED.toString())
                .replace("{purple}", ChatColor.LIGHT_PURPLE.toString())
                .replace("{light_purple}", ChatColor.LIGHT_PURPLE.toString())
                .replace("{yellow}", ChatColor.YELLOW.toString())
                .replace("{white}", ChatColor.WHITE.toString())

                //style
                .replace("{magic}", ChatColor.MAGIC.toString())
                .replace("{bold}", ChatColor.BOLD.toString())
                .replace("{delete}", ChatColor.STRIKETHROUGH.toString())
                .replace("{underline}", ChatColor.UNDERLINE.toString())
                .replace("{italic}", ChatColor.ITALIC.toString())
                .replace("{reset}", ChatColor.RESET.toString())

                //return
                .replace("{return}", "\n")

                //query
                .replace("{player}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max_player}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{tps}", s2)
                .replace("{date}", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    static String formatPlayerHolder(Player player, String template) {
        return template.replace("{name}", player.getName())
                .replace("{address}", player.getAddress().toString());
    }

    static int getBukkitVersion() {
        return Integer.parseInt(Bukkit.getServer().getBukkitVersion().split("-")[0].split("\\.")[1]);
    }

    static String encrypt(Player p) {
        return encrypt(p.getName());
    }

    static String encrypt(String name) {
        HashFunction func = Hashing.sha512();
        HashCode hashCode = func.hashString(name, StandardCharsets.UTF_8);
        return hashCode.toString();
    }

    static int getPing(Player player) {
        try {
            Class<?> c = Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer");
            Class<?> c2 = Class.forName("net.minecraft.world.entity.EntityPlayer");

            return (int) c2.getField("ping").get(c.getMethod("getHandle").invoke(c.cast(player)));
        } catch (ClassNotFoundException | NoSuchFieldException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static CommandMap getAndFuckCommandMap() {
        Class<?> c = Bukkit.getServer().getClass();
        try {
            return (CommandMap) c.getMethod("getCommandMap").invoke(Bukkit.getServer(), new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void init() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Quark.PLUGIN, new TPSCalcTask(), 0, 1);
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

    static void banPlayer(String name, int day, int hour, int minute, int second, String reason) {

    }

    static String getLocaleId(Locale locale) {
        return fixLocaleId(locale.toString().toLowerCase());
    }

    static PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        Validate.notNull(file, "File cannot be null");

        JarFile jar = null;
        InputStream stream = null;

        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }

            stream = jar.getInputStream(entry);

            PluginDescriptionFile f = new PluginDescriptionFile(stream);
            stream.close();
            return f;

        } catch (IOException | YAMLException ex) {
            throw new InvalidDescriptionException(ex);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ignored) {
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    static String fixLocaleId(String locale) {
        if (locale == null) {
            return "zh_cn";
        }
        if (locale.startsWith("zh")) {
            return locale.toLowerCase();
        }
        return locale.split("_")[0];
    }


    final class TPSCalcTask implements Runnable {
        long sec;
        long currentSec;
        int ticks;
        int tps;
        int i;

        @Override
        public void run() {
            if (i == 0) {
                i++;
            } else {
                sec = (System.currentTimeMillis() / 1000);
                if (currentSec == sec) {
                    ticks++;
                } else {
                    currentSec = sec;
                    tps = (tps == 0 ? ticks : ((tps + ticks) / 2));
                    TPS.set(tps);
                    ticks = 0;
                }
            }
        }
    }
}
