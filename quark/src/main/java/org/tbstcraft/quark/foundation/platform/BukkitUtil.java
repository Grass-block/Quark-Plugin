package org.tbstcraft.quark.foundation.platform;

import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRS0;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.internal.task.TaskService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "Convert2MethodRef"})
public interface BukkitUtil {
    MethodHandleRS0<double[]> TPS = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Bukkit.class.getMethod("getTPS"), () -> Bukkit.getTPS());
        ctx.attempt(() -> Server.class.getMethod("getTPS"), () -> Bukkit.getServer().getTPS());
        ctx.dummy(() -> new double[]{20.0});
    });
    MethodHandleRS0<Double> MSPT = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Bukkit.class.getMethod("getAverageTickTime"), () -> Bukkit.getAverageTickTime());
        ctx.attempt(() -> Server.class.getMethod("getAverageTickTime"), () -> Bukkit.getServer().getAverageTickTime());
        ctx.dummy(() -> 0.0);
    });

    //----[API]----
    static double getTPS() {
        return TPS.invoke()[0];
    }

    static double getMSPT() {
        return MSPT.invoke();
    }

    @SafeVarargs
    static <E extends Event> void callEvent(E event, Consumer<E>... outcomes) {
        Runnable command = () -> {
            Bukkit.getPluginManager().callEvent(event);

            for (Consumer<E> outcome : outcomes) {
                outcome.accept(event);
            }
        };

        if (event.isAsynchronous()) {
            TaskService.async().run(command);
        } else {
            TaskService.global().run(command);
        }
    }

    static void registerEventListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Quark.getInstance());
    }

    static void unregisterEventListener(Listener listener) {
        try {
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
                    Quark.getInstance().getLogger().warning("failed to unregister listener %s: %s".formatted(m.getName(), e.getMessage()));
                    continue;
                }
                list.unregister(listener);
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }


    //----[Calculate]----
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

    static double getMinimumAxis(Vector vector) {
        return Math.min(Math.min(Math.abs(vector.getX()), Math.abs(vector.getY())), Math.abs(vector.getZ()));
    }

    static double getMaximumAxis(Vector vector) {
        return Math.max(Math.max(Math.abs(vector.getX()), Math.abs(vector.getY())), Math.abs(vector.getZ()));
    }


    //----[Format]----
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
        return col + new DecimalFormat("0.00").format(tps);
    }

    static String formatMSPT(double mspt) {
        ChatColor col = (mspt < 15 ? ChatColor.GREEN : mspt < 35 ? ChatColor.YELLOW : ChatColor.RED);
        return col + new DecimalFormat("0.00").format(mspt);
    }


    //----[Create]----
    static ItemStack createStack(Material material, int amount, String name) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(TextBuilder.buildComponent(name));
        }
        stack.setItemMeta(meta);
        return stack;
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
}
