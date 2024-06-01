package org.tbstcraft.quark.util.platform;

import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.text.TextBuilder;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings({"unused", "deprecation"})
public interface BukkitUtil {
    String NAMESPACE = ChatColor.DARK_GRAY + "quark::";

    static double getTPS() {
        try {
            Object o = Bukkit.class.getDeclaredMethod("getTPS").invoke(null);
            return ((double[]) o)[0];
        } catch (Exception e) {
            return 20;
        }
    }

    static double getMSPT() {
        try {
            return (double) Bukkit.class.getDeclaredMethod("getAverageTickTime").invoke(null);
        } catch (Exception e) {
            return -1;
        }
    }

    static void registerEventListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Quark.PLUGIN);
    }

    static void unregisterEventListener(Listener listener) {
        for (Method m : listener.getClass().getMethods()) {
            EventHandler handler = m.getDeclaredAnnotation(EventHandler.class);
            if (handler == null) {
                continue;
            }
            HandlerList list;
            try {
                Class<?> clazz = m.getParameters()[0].getType();

                list = (HandlerList) clazz.getMethod("getHandlerList").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                Quark.LOGGER.warning("failed to unregister listener %s: %s".formatted(m.getName(), e.getMessage()));
                continue;
            }
            list.unregister(listener);
        }
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

    static boolean testJump(Location loc, Location old) {
        if (loc.getY() - old.getY() <= 0) {
            return false;
        }
        return (!onGround(loc)) && onGround(old);
    }

    static boolean onGround(Location loc) {
        Block b = getSteppingBlock(loc);
        if (b == null) {
            return false;
        }
        return b.getType().isSolid();
    }

    static Block getSteppingBlock(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return null;
        }

        int x = loc.getBlockX();
        int y = (int) (Math.ceil(loc.getY())) - 1;
        int z = loc.getBlockZ();
        return world.getBlockAt(x, y, z);
    }

    static Object toCraftEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

            String entityName = entity.getClass().getSimpleName().replace("Craft", "");
            String typeName = "org.bukkit.craftbukkit." + version + ".entity.Craft" + entityName;
            if (!entity.getClass().getName().equals(typeName)) {
                entity = Bukkit.getEntity(entity.getUniqueId());
            }
            Class<?> craftEntityType = Class.forName(typeName);
            return craftEntityType.cast(entity);
        } catch (Exception e) {
            Quark.LOGGER.severe(e.getMessage());
            return null;
        }
    }

    static double getMinimumAxis(Vector vector) {
        return Math.min(Math.min(Math.abs(vector.getX()), Math.abs(vector.getY())), Math.abs(vector.getZ()));
    }

    static double getMaximumAxis(Vector vector) {
        return Math.max(Math.max(Math.abs(vector.getX()), Math.abs(vector.getY())), Math.abs(vector.getZ()));
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

    static ItemStack createStack(Material material, int amount, String name) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(TextBuilder.buildComponent(name));
        }
        stack.setItemMeta(meta);
        return stack;
    }


    static void sendConsoleMessageBlock(TextComponent component) {
        Bukkit.getConsoleSender().sendMessage(component);
    }


    static String formatPing(int ping) {
        StringBuilder sb = new StringBuilder();
        if (ping < 75) {
            sb.append(ChatColor.GREEN);
        } else if (ping < 250) {
            sb.append(ChatColor.YELLOW);
        } else {
            sb.append(ChatColor.RED);
        }
        sb.append(ping);
        sb.append(ChatColor.RESET);
        return sb.toString();
    }

    static String formatTPS(double tps) {
        ChatColor col = (tps > 17 ? ChatColor.GREEN : tps > 10 ? ChatColor.YELLOW : ChatColor.RED);
        return col + new DecimalFormat("#.00").format(tps);
    }

    static String formatMSPT(double mspt) {
        ChatColor col = (mspt < 15 ? ChatColor.GREEN : mspt < 35 ? ChatColor.YELLOW : ChatColor.RED);
        return col + new DecimalFormat("#.00").format(mspt);
    }

    static void createDrop(Location loc, ItemStack item) {
        Random r = new Random();

        double dx = r.nextDouble(-0.05, 0.05);
        double dy = r.nextDouble(0.05, 0.35);
        double dz = r.nextDouble(-0.05, 0.05);

        loc.add(dx + 0.5, dy + 0.1, dz + 0.5);
        Item e = loc.getWorld().spawn(loc, Item.class);
        e.setItemStack(Objects.requireNonNull(item));
    }


    static void createPermission(String perm) {
        String id = perm.substring(1);
        Permission node = new Permission(id, switch (perm.charAt(0)) {
            case '+' -> PermissionDefault.TRUE;
            case '-' -> PermissionDefault.OP;
            case '!' -> PermissionDefault.FALSE;
            default -> throw new RuntimeException("invalid value:" + perm.charAt(0));
        });
        if (Bukkit.getPluginManager().getPermission(id) != null) {
            return;
        }
        Bukkit.getPluginManager().addPermission(node);
    }

    static String getItemUsage(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String s : lore) {
                    if (s.startsWith(NAMESPACE)) {
                        return s.split("::")[1];
                    }
                }
            }
        }
        return "";
    }


    static <E extends Event> E callEvent(E event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    static <E extends Event> E callEventAsync(E event) {
        try {
            Method m = Bukkit.getPluginManager().getClass().getDeclaredMethod("fireEvent", Event.class);
            m.setAccessible(true);
            m.invoke(Bukkit.getPluginManager(), event);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return event;
    }
}
