package org.tbstcraft.quark;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.backend.MessageBackend;
import me.gb2022.commons.Timer;
import net.kyori.adventure.text.ComponentLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.PluginConcept;
import org.atcraftmc.qlib.PluginPlatform;
import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.config.Queries;
import org.atcraftmc.qlib.config.YamlUtil;
import org.atcraftmc.qlib.language.ILanguageAccess;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.api.CoreEvent;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.foundation.command.QuarkCommandManager;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.metrics.Metrics;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.jar.JarFile;

/**
 * <h3>江城子·腐竹之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万插件，Bug何处藏。纵使上线又何妨，朝要改，夕断肠。<br>
 * 玩家每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Quark extends JavaPlugin implements PluginConcept {
    public static final Logger LOGGER = LogManager.getLogger("Quark");
    public static final int API_VERSION = 40;
    public static final int BSTATS_ID = 22683;
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm9";

    public static final ILanguageAccess LANGUAGE = LanguageContainer.getInstance().access("quark-core");
    public static Quark PLUGIN;

    private final BundledPackageProvider bundledPackageLoader = new BundledPackageProvider();
    private final CommandManager commandManager = new QuarkCommandManager(this);

    private String uuid;
    private Metrics metrics;
    private boolean fastBoot;
    private boolean initialized;
    private boolean hasBundler = false;

    public static void reload(CommandSender audience) {
        BukkitUtil.callEventDirect(new CoreEvent.Reload());

        Runnable task = () -> {
            try {
                Locale locale = LocaleService.locale(audience);
                String msg = LANGUAGE.getMessage(locale, "packages", "load");

                var serverPacks = PackageManager.getSubPacksFromServer();
                var folderPacks = PackageManager.getSubPacksFromFolder();
                var modernPluginManager = PluginUtil.INSTANCE;
                var coreFile = modernPluginManager.getFile(PLUGIN_ID);

                modernPluginManager.unload(PLUGIN_ID);

                for (var id : serverPacks) {
                    modernPluginManager.unload(id);
                }

                modernPluginManager.load(coreFile);

                for (var file : folderPacks) {
                    modernPluginManager.load(file);
                }

                audience.sendMessage(msg);

                LegacyCommandManager.sync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        task.run();

        BukkitUtil.callEventDirect(new CoreEvent.PostReload());
    }

    public static Quark getInstance() {
        return (Quark) Bukkit.getPluginManager().getPlugin("quark");
    }

    private void operation(String operation, Runnable task) {
        LOGGER.info(operation);
        task.run();
    }

    private void operation(String operation, boolean condition, Runnable task) {
        if (!condition) {
            return;
        }
        operation(operation, task);
    }

    //----[plugin concept]----
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    @Override
    public String folder() {
        return getDataFolder().getAbsolutePath();
    }

    @Override
    public String configId() {
        return "quark-core";
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

    private void initializePluginEnv() {
        LOGGER.info("Plugin Environment: ");

        APIProfileTest.test();
        var threadedRegions = APIProfileTest.isFoliaServer();
        var modded = APIProfileTest.isMixedServer();
        this.uuid = UUID.randomUUID().toString();
        this.hasBundler = this.bundledPackageLoader.isPresent();
        PLUGIN = this;
        PluginUtil.CORE_REF.set(this);
        PluginPlatform.setPlatform(new Q_BukkitPlatform());

        LOGGER.info(" - platform: {}", APIProfileTest.getAPIProfile().toString());
        LOGGER.info(" - region scheduler: {}", threadedRegions);
        LOGGER.info(" - modded environment: {}", modded);
        LOGGER.info(" - instance UUID: {}", this.uuid);
        LOGGER.info(" - qlib environment: {}", PluginPlatform.instance().getClass());
        LOGGER.info(" - bundler mode: {}", this.hasBundler);

        if (threadedRegions) {
            LOGGER.warn("detected Folia type(Threaded Regions API) environment. using threadedRegions task system.");
        }
        if (modded) {
            LOGGER.warn("detected Arclight type(Forge API) environment. try restart your server rather than /quark reload.");
        }
        if (isFastBoot() && modded) {
            LOGGER.warn("fastboot are not available on Arclight type(Forge API) platform! disabling fast-boot.");
            this.fastBoot = false;
        }
        if (isFastBoot()) {
            LOGGER.warn("using Fast-Boot environment, hot-reload may not function well. RESTART your server if any error occurred.");
        }
    }

    private void initializeCoreConfiguration() {
        this.saveDefaultConfig();
        this.reloadConfig();

        var config = getConfig();
        var metrics = config.getBoolean("config.plugin.metrics");
        this.fastBoot = config.getBoolean("config.plugin.fast-boot");

        try {
            ProductInfo.METADATA.load(getClass().getClassLoader().getResourceAsStream("product-info.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        var templateResource = Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"));
        var template = YamlConfiguration.loadConfiguration(new InputStreamReader(templateResource));

        YamlUtil.update(config, template, false, 3);

        saveConfig();

        Queries.setEnvironmentVars(Objects.requireNonNull(config.getConfigurationSection("config.environment")));

        LOGGER.info(" - core version: {}[{}]", ProductInfo.version(), API_VERSION);
        LOGGER.info(" - fast boot: {}", this.fastBoot);
        LOGGER.info(" - metrics: {}", metrics);

        if (metrics) {
            this.metrics = new Metrics(this, BSTATS_ID);
        }
    }

    private void loadFullJar() {
        LOGGER.info("loading full jar...");

        var pluginsPath = System.getProperty("user.dir") + "/plugins/";
        var pluginsDir = new File(pluginsPath);
        var loader = Quark.class.getClassLoader();

        File jar = null;
        for (File f : Objects.requireNonNull(pluginsDir.listFiles())) {
            if (f.isDirectory() || !f.getName().endsWith(".jar")) {
                continue;
            }

            try {
                if (PluginUtil.getPluginDescription(f).getName().equals(PLUGIN_ID)) {
                    jar = f;
                }
            } catch (InvalidDescriptionException ignored) {
            }
        }

        if (jar == null) {
            throw new RuntimeException("cannot find plugin!");
        }

        LOGGER.info("target core jar file: {}", jar.getAbsolutePath());

        var counter = 0;

        try (JarFile jarFile = new JarFile(jar)) {
            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                var className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");

                try {
                    loader.loadClass(className);
                    counter++;
                } catch (Throwable e) {
                    LOGGER.warn("failed to load class {}: {}", className, e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("loaded {} classes.", counter);
    }

    //----[plugin]----
    @Override
    public void onEnable() {
        Timer.restartTiming();

        for (String s : ProductInfo.logo(this).split("\n")) {
            Bukkit.getConsoleSender().sendMessage(s);
        }

        this.initializePluginEnv();
        this.initializeCoreConfiguration();

        BukkitUtil.callEventDirect(new CoreEvent.Launch(this));

        if (!this.fastBoot) {
            this.loadFullJar();
        }

        operation("starting services...", () -> {
            Service.initBase();
            QuarkInternalPackage.register(PackageManager.INSTANCE.get());

            ClientMessenger.setBackend(MessageBackend.bukkit(Quark.getInstance()));
            ClientMessenger.getBackend().start();
        });

        if (this.hasBundler) {
            LOGGER.info("loading bundled packs...");
            this.bundledPackageLoader.onEnable();
        }

        LOGGER.info("done. ({} ms)", Timer.passedTime());
        this.initialized = true;

        BukkitUtil.callEventDirect(new CoreEvent.PostLaunch(this));
    }

    @Override
    public void onDisable() {
        this.initialized = false;

        BukkitUtil.callEventDirect(new CoreEvent.Dispose(this));
        Timer.restartTiming();
        LOGGER.info("stopping(v{}-{})...", ProductInfo.version(), API_VERSION);

        if (this.hasBundler) {
            LOGGER.info("unloading bundled packs...");
            this.bundledPackageLoader.onDisable();
        }

        operation("stopping services...", () -> {
            ServiceManager.unregisterAll();
            Service.stopBase();
            ClientMessenger.getBackend().stop();
        });
        operation("destroying context...", () -> {
            try {
                this.getMetrics().shutdown();
            } catch (Exception ignored) {
            }

            this.metrics = null;
        });
        operation("running finalize tasks...", TaskService::runFinalizeTask);

        LOGGER.info("done ({} ms)", Timer.passedTime());

        BukkitUtil.callEventDirect(new CoreEvent.PostDispose(this));
    }

    @Override
    public @NotNull File getFile() {
        return super.getFile();
    }

    //----[properties]----
    public String getInstanceUUID() {
        return this.uuid;
    }

    public boolean isFastBoot() {
        return this.fastBoot;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public boolean isPluginInitialized() {
        return initialized;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }


    private static final class Q_BukkitPlatform implements PluginPlatform {
        @Override
        public void sendMessage(Object o, ComponentLike componentLike) {
            TextSender.sendMessage(((CommandSender) o), componentLike);
        }

        @Override
        public Locale locale(Object o) {
            return LocaleService.locale(((CommandSender) o));
        }

        @Override
        public String globalFormatMessage(String s) {
            return PlaceHolderService.format(PlaceHolderService.format(s));
        }

        @Override
        public void broadcastLine(Function<Locale, ComponentLike> function, boolean b, boolean b1) {
            TextSender.broadcastLine(function, b, b1);
        }

        @Override
        public PluginConcept defaultPlugin() {
            return Quark.PLUGIN;
        }

        @Override
        public String pluginsFolder() {
            return FilePath.pluginsFolder();
        }
    }

    public static final class PluginConceptWrapper implements PluginConcept {
        private final Object handle;

        private PluginConceptWrapper(Object handle) {
            this.handle = handle;
        }

        public static PluginConcept of(Plugin owner) {
            return new PluginConceptWrapper(owner);
        }

        @Override
        public String id() {
            return PLUGIN_ID;
        }

        @Override
        public String folder() {
            return PLUGIN.getDataFolder().getAbsolutePath();
        }

        @Override
        public String configId() {
            return "quark-core";
        }

        @Override
        public Logger logger() {
            return LOGGER;
        }

        @Override
        public Object handle() {
            return this.handle;
        }
    }
}
