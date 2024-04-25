package org.tbstcraft.quark.util.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;

public interface BukkitPluginManager {
    HashMap<String, String> CACHE = new HashMap<>();

    static boolean load(String file) {
        try {
            File f = new File(System.getProperty("user.dir") + "/plugins/" + file);
            Plugin p;
            try {
                unload(BukkitUtil.getPluginDescription(f).getName());
                p = Bukkit.getPluginManager().loadPlugin(f);
            } catch (InvalidPluginException | InvalidDescriptionException e) {
                Quark.LOGGER.severe(e.getMessage());
                return false;
            }
            if (p == null) {
                return false;
            }
            if (Bukkit.getPluginManager().isPluginEnabled(p.getName())) {
                return false;
            }
            p.onLoad();
            Bukkit.getPluginManager().enablePlugin(p);

        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    static boolean unload(String id) {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Plugin target = pluginManager.getPlugin(id);
            if (target == null) {
                return false;
            }
            if (!pluginManager.isPluginEnabled(target)) {
                return false;
            }
            pluginManager.disablePlugin(target);

            ClassLoader cl = target.getClass().getClassLoader();
            Class<?> pluginManagerClass = Bukkit.getPluginManager().getClass();

            try {
                try {
                    Field subManager = pluginManagerClass.getDeclaredField("paperPluginManager");
                    subManager.setAccessible(true);
                    PluginManager paper = (PluginManager) subManager.get(pluginManager);

                    Field instanceManager = paper.getClass().getDeclaredField("instanceManager");
                    instanceManager.setAccessible(true);

                    Class<?> paperInstanceManagerClass = Class.forName("io.papermc.paper.plugin.manager.PaperPluginInstanceManager");
                    clearPluginInstances(paperInstanceManagerClass, instanceManager.get(paper), target);
                } catch (Exception ignored) {
                }
                clearPluginInstances(pluginManagerClass, pluginManager, target);

                if (cl instanceof URLClassLoader) {
                    Field pluginInit = cl.getClass().getDeclaredField("pluginInit");
                    pluginInit.setAccessible(true);
                    pluginInit.set(cl, null);

                    Field plugin = cl.getClass().getDeclaredField("plugin");
                    plugin.setAccessible(true);
                    plugin.set(cl, null);

                    try {
                        ((URLClassLoader) cl).close();
                    } catch (IOException e) {
                        Quark.LOGGER.warning("failed to close resource: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Quark.LOGGER.warning("failed to close pluginLoader: " + e.getMessage());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    static void clearPluginInstances(Class<?> clazz, Object instance, Plugin target) {
        try {
            Field lookupNamesField = clazz.getDeclaredField("lookupNames");
            Field pluginsField = clazz.getDeclaredField("plugins");
            lookupNamesField.setAccessible(true);
            pluginsField.setAccessible(true);
            Map lookup = ((Map) lookupNamesField.get(instance));
            List plugins = (List) pluginsField.get(instance);
            lookup.remove(target.getName());
            plugins.remove(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void reload(String id) {
        unload(id);
        load(Objects.requireNonNull(getPluginJar(id)).getName());
    }

    static File getPluginJar(String id) {
        updateMapping();
        return new File(CACHE.get(id));
    }

    static void updateMapping() {
        for (String s : CACHE.keySet()) {
            File f = new File(CACHE.get(s));
            if (f.exists()) {
                continue;
            }
            CACHE.remove(s);
        }
        for (File f : getAllPluginFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            if (CACHE.containsValue(f.getAbsolutePath())) {
                continue;
            }
            String pluginName;
            try {
                pluginName = BukkitUtil.getPluginDescription(f).getName();
            } catch (InvalidDescriptionException e) {
                throw new RuntimeException(e);
            }
            CACHE.put(pluginName, f.getAbsolutePath());
        }
    }

    static List<File> getAllPluginFiles() {
        List<File> lists = new ArrayList<>();
        for (File f : Objects.requireNonNull(new File(FilePath.server() + "/plugins").listFiles())) {
            if (f.isDirectory()) {
                continue;
            }
            lists.add(f);
        }
        return lists;
    }
}
