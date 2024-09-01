package org.tbstcraft.quark.foundation.platform;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.*;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;
import me.gb2022.commons.container.ObjectContainer;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings({"removal"})
public interface PluginUtil {
    HashMap<String, String> CACHE = new HashMap<>();
    ObjectContainer<Quark> CORE_REF = new ObjectContainer<>();

    static Plugin load(String file) {
        try {
            File f = new File(System.getProperty("user.dir") + "/plugins/" + file);
            Plugin p;
            try {
                unload(getPluginDescription(f).getName());

                if (APIProfileTest.isArclightBasedServer()) {
                    p = CORE_REF.get().getPluginLoader().loadPlugin(f);
                } else {
                    p = Bukkit.getPluginManager().loadPlugin(f);
                }

            } catch (InvalidPluginException | InvalidDescriptionException e) {
                Quark.getInstance().getLogger().severe(e.getMessage());
                return null;
            }
            if (p == null) {
                return null;
            }
            if (Bukkit.getPluginManager().isPluginEnabled(p.getName())) {
                return null;
            }
            p.onLoad();
            Bukkit.getPluginManager().enablePlugin(p);
            return p;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
                        Quark.getInstance().getLogger().warning("failed to close resource: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Quark.getInstance().getLogger().warning("failed to close pluginLoader: " + e.getMessage());
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

    static Plugin reload(String id) {
        unload(id);
        return load(Objects.requireNonNull(getPluginJar(id)).getName());
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
                pluginName = getPluginDescription(f).getName();
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
}
