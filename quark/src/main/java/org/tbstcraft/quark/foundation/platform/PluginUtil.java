package org.tbstcraft.quark.foundation.platform;

import me.gb2022.commons.container.ObjectContainer;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.*;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

@SuppressWarnings({"removal", "CallToPrintStackTrace"})
public interface PluginUtil {
    ModernPluginManager INSTANCE = ModernPluginManager.getInstance(Quark.getInstance());
    ObjectContainer<Quark> CORE_REF = new ObjectContainer<>();

    static Plugin load(String fileName) {
        File file = new File(System.getProperty("user.dir") + "/plugins/" + fileName);
        return INSTANCE.load(file);
    }

    static boolean unload(String id) {
        return INSTANCE.unload(id);
    }

    static Plugin reload(String id) {
        return INSTANCE.reload(id);
    }

    static File getPluginJar(String id) {
        return INSTANCE.getFile(id);
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

    /**
     * Bukkit implemented description reading
     *
     * @param file specified file
     * @return Plugin description file
     * @throws InvalidDescriptionException Bukkit logic
     */
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

    @SuppressWarnings("JavaReflectionInvocation")
    interface PluginCleanup {
        static boolean isPaperPluginEcosystemPresent() {
            try {
                Class.forName("io.papermc.paper.plugin.entrypoint.strategy.modern.ModernPluginLoadingStrategy");
                Class.forName("io.papermc.paper.plugin.manager.PaperPluginInstanceManager");
                return true;
            } catch (Throwable e) {
                return false;
            }
        }

        static Object getFieldValue(Object instance, String fieldName) throws NoSuchFieldException, IllegalAccessException {
            var f = instance.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(instance);
        }

        static void clearPluginHoldings(Object holder, Plugin plugin) throws NoSuchFieldException, IllegalAccessException {
            var plugins = (List<Plugin>) getFieldValue(holder, "plugins");
            var lookupNames = (Map<String, Plugin>) getFieldValue(holder, "lookupNames");

            plugins.removeIf(pluginInstance -> pluginInstance == plugin || pluginInstance.getName().equals(plugin.getName()));

            for (var k : List.copyOf(lookupNames.keySet())) {
                if (lookupNames.get(k).equals(plugin)) {
                    lookupNames.remove(k);
                }
                if (k.equals(plugin.getName())) {
                    lookupNames.remove(k);
                }
            }
        }

        static void unloadPaperPluginStorage(PluginManager pluginManager, Plugin plugin) throws Exception {
            if (!isPaperPluginEcosystemPresent()) {
                return;
            }

            try {
                var paperPluginManager = getFieldValue(pluginManager, "paperPluginManager");
                var instanceManager = getFieldValue(paperPluginManager, "instanceManager");

                clearPluginHoldings(instanceManager, plugin);
            } catch (IllegalStateException | NoClassDefFoundError ignored) {
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        static void closePluginClassLoader(Plugin plugin) throws Exception {
            ClassLoader cl = plugin.getClass().getClassLoader();
            if (!(cl instanceof URLClassLoader)) {
                return;
            }

            var f_pluginInit = cl.getClass().getDeclaredField("pluginInit");
            var f_plugin = cl.getClass().getDeclaredField("plugin");

            f_pluginInit.setAccessible(true);
            f_pluginInit.set(cl, null);

            f_plugin.setAccessible(true);
            f_plugin.set(cl, null);

            try {
                ((URLClassLoader) cl).close();
            } catch (IOException e) {
                Quark.getInstance().getLogger().warning("failed to close resource: " + e.getMessage());
            }
        }

        /**
         * <code>LaunchEntryPointHandler.INSTANCE.get(Entrypoint.PLUGIN).getRegisteredProviders().iterator().removeIf();</code><br>
         * I know its stupid, but this is possible
         */
        static void unloadPaperPluginProvider(String id) throws Exception {
            if (!isPaperPluginEcosystemPresent()) {
                return;
            }

            try {
                Class.forName("io.papermc.paper.plugin.entrypoint.strategy.modern.ModernPluginLoadingStrategy");

                var c_LaunchEntryPointProvider = Class.forName("io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler");
                var c_EntryPoint = Class.forName("io.papermc.paper.plugin.entrypoint.Entrypoint");
                var c_ProviderStorage = Class.forName("io.papermc.paper.plugin.storage.ProviderStorage");
                var c_PluginProvider = Class.forName("io.papermc.paper.plugin.provider.PluginProvider");
                var c_PluginMeta = Class.forName("io.papermc.paper.plugin.configuration.PluginMeta");

                var m_getRegisteredProvider = c_ProviderStorage.getMethod("getRegisteredProviders");
                var m_get = c_LaunchEntryPointProvider.getDeclaredMethod("get", c_EntryPoint);
                var m_getMeta = c_PluginProvider.getDeclaredMethod("getMeta");
                var m_getName = c_PluginMeta.getDeclaredMethod("getName");

                var launchEntryPointHandler = c_LaunchEntryPointProvider.getDeclaredField("INSTANCE").get(null);
                var pluginEntryPoint = c_EntryPoint.getDeclaredField("PLUGIN").get(null);
                var pluginProviderStorage = m_get.invoke(launchEntryPointHandler, pluginEntryPoint);
                var registeredProviders = ((List<?>) m_getRegisteredProvider.invoke(pluginProviderStorage));

                var iterator = registeredProviders.iterator();

                while (iterator.hasNext()) {
                    var provider = iterator.next();

                    var meta = m_getMeta.invoke(provider);
                    var name = m_getName.invoke(meta);

                    if (name.equals(id)) {
                        iterator.remove();
                    }
                }

            } catch (ClassNotFoundException | IllegalStateException | NoClassDefFoundError ignored) {
            } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    final class ModernPluginManager {
        private final HashMap<String, File> pluginFileMapping = new HashMap<>();
        private final Logger logger;
        private final Plugin owner;
        private final PluginManager handle;

        public ModernPluginManager(Logger logger, Plugin owner, PluginManager handle) {
            this.logger = logger;
            this.owner = owner;
            this.handle = handle;
        }

        static ModernPluginManager getInstance(Plugin owner) {
            return new ModernPluginManager(owner.getLogger(), owner, Bukkit.getPluginManager());
        }

        public File getFile(String id) {
            if (this.pluginFileMapping.containsKey(id)) {
                var f = this.pluginFileMapping.get(id);
                if (f.exists()) {
                    return f;
                }

                this.pluginFileMapping.remove(id);
                return null;
            }

            for (var f : getAllPluginFiles()) {
                if (f.isDirectory()) {
                    continue;
                }

                if (this.pluginFileMapping.containsValue(f)) {
                    continue;
                }

                try {
                    this.pluginFileMapping.put(getPluginDescription(f).getName(), f);
                } catch (InvalidDescriptionException e) {
                    this.owner.getLogger().warning("find invalid plugin file: " + f.getName());
                }
            }

            return this.pluginFileMapping.get(id);
        }

        public Plugin load(String id) {
            var file = getFile(id);

            if (file == null) {
                this.logger.severe("cannot find plugin named %s".formatted(id));
                return null;
            }

            return this.load(file);
        }

        public Plugin reload(String id) {
            var plugin = Bukkit.getPluginManager().getPlugin(id);

            if (plugin == null) {
                this.logger.severe("cannot find plugin handle of: " + id);
                return null;
            }

            return this.reload(plugin);
        }

        public Plugin reload(Plugin plugin) {
            var id = plugin.getName();
            var file = getFile(id);

            if (!this.unload(id)) {
                this.logger.severe("cannot unload plugin: " + id);
                return plugin;
            }
            return this.load(file);
        }

        public boolean unload(String id) {
            var plugin = Bukkit.getPluginManager().getPlugin(id);

            if (plugin == null) {
                this.logger.severe("cannot find plugin handle of: " + id);
                throw new NullPointerException("cannot find plugin handle of: " + id);
            }

            return unload(plugin);
        }

        public File getFile(Plugin plugin) {
            return getFile(plugin.getName());
        }

        public Plugin load(File file) {
            String id;
            try {
                id = getPluginDescription(file).getName();
            } catch (InvalidDescriptionException e) {
                return null;
            }

            if (this.handle.getPlugin(id) != null) {
                return this.handle.getPlugin(id);
            }


            Plugin p;
            try {
                if (APIProfileTest.isArclightBasedServer()) {
                    p = CORE_REF.get().getPluginLoader().loadPlugin(file);
                } else {
                    p = this.handle.loadPlugin(file);
                }

            } catch (InvalidPluginException | InvalidDescriptionException e) {
                Quark.getInstance().getLogger().severe(e.getMessage());
                return null;
            }
            if (p == null) {
                this.logger.severe("cannot load plugin '%s' whatever".formatted(id));
                return null;
            }

            try {
                p.onLoad();
            } catch (Throwable e) {
                this.logger.severe("find exception when loading plugin '%s':".formatted(id));
                e.printStackTrace();
            }

            try {
                this.handle.enablePlugin(p);
            } catch (Throwable e) {
                this.logger.severe("find exception when enabling plugin '%s':".formatted(id));
                e.printStackTrace();
            }

            return p;
        }

        public boolean unload(Plugin plugin) {
            if (plugin == null) {
                return false;
            }

            var id = plugin.getName();

            if (!this.handle.isPluginEnabled(plugin)) {
                this.logger.warning("disabled plugin unloading request of plugin: '" + id + "'");
                return false;
            }

            try {
                this.handle.disablePlugin(plugin);
            } catch (Exception e) {
                this.logger.severe("found exception when disabling plugin " + plugin.getName());
                e.printStackTrace();
                return false;
            }

            try {
                PluginCleanup.clearPluginHoldings(this.handle, plugin);
                PluginCleanup.unloadPaperPluginStorage(this.handle, plugin);
                PluginCleanup.closePluginClassLoader(plugin);
                PluginCleanup.unloadPaperPluginProvider(id);

                return true;
            } catch (Exception e) {
                this.logger.severe("found exception when cleaning plugin instance of" + plugin.getName());
                e.printStackTrace();
                return false;
            }
        }
    }
}
