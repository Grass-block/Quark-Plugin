package org.atcraftmc.starlight;

import me.gb2022.commons.Timer;
import me.gb2022.pluginsX.PluginService;
import net.kyori.adventure.text.ComponentLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.PluginConcept;
import org.atcraftmc.qlib.bukkit.BukkitPlatform;
import org.atcraftmc.qlib.bukkit.BukkitPluginConcept;
import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.atcraftmc.qlib.config.Queries;
import org.atcraftmc.qlib.config.YamlUtil;
import org.atcraftmc.qlib.language.LanguageAccess;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.qlib.platform.ForwardingPluginPlatform;
import org.atcraftmc.qlib.platform.PluginPlatform;
import org.atcraftmc.starlight.api.event.CoreEvent;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.util.dependency.LibraryManager;
import org.atcraftmc.starlight.util.dependency.MavenRepo;
import org.atcraftmc.starlight.foundation.TextExaminer;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.PluginUtil;
import org.atcraftmc.starlight.framework.packages.PackageManager;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceLayer;
import org.atcraftmc.starlight.framework.service.ServiceManager;
import org.atcraftmc.starlight.internal.command.InternalCommands;
import org.atcraftmc.starlight.metrics.Metrics;
import org.atcraftmc.starlight.util.FilePath;
import org.atcraftmc.starlight.util.ProductMetadata;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarFile;

/**
 * <h3>江城子·腐竹之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万插件，Bug何处藏。纵使上线又何妨，朝要改，夕断肠。<br>
 * 玩家每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Starlight extends BukkitPluginConcept {
    public static final Logger LOGGER = LogManager.getLogger("Starlight-Core");
    public static Starlight PLUGIN;
    public static LanguageAccess LANGUAGE;
    public final LanguageContainer language = new LanguageContainer(this, ProductInfo.CORE_ID);
    private final BundledPackageProvider bundledPackageLoader = new BundledPackageProvider();
    private final CommandManager commandManager = new StarlightCommandManager(this);
    private final ProductMetadata metadata = ProductMetadata.createFromResource(this);
    private LibraryManager libraryManager;
    private String uuid;
    private Metrics metrics;
    private boolean fastBoot;
    private boolean initialized;
    private boolean hasBundler = false;
    private boolean debug = true;

    public static void reload(CommandSender audience) {
        BukkitUtil.callEventDirect(new CoreEvent.Reload());

        var task = (Runnable) () -> {
            var manager = Bukkit.getPluginManager();
            var holder = manager.getPlugins()[0]; //not good but maybe possible
            var service = new PluginService(manager, holder);

            var locale = LocaleService.locale(audience);
            var msg = Starlight.instance().language().item("starlight-core.reload.complete").component(locale);

            var serverPacks = PackageManager.getSubPacksFromServer();
            var folderPacks = PackageManager.getSubPacksFromFolder();
            var modernPluginManager = PluginUtil.INSTANCE;
            var coreFile = modernPluginManager.getFile(ProductInfo.CORE_ID);

            for (var id : serverPacks) {
                service.unload(id);
            }

            service.unload(ProductInfo.CORE_ID);

            service.load(coreFile);

            for (var file : folderPacks) {
                service.load(file);
            }

            audience.sendMessage(msg);

            LegacyCommandManager.sync();
        };

        task.run();

        BukkitUtil.callEventDirect(new CoreEvent.PostReload());
    }

    public static Starlight instance() {
        return PLUGIN;
    }

    public static LanguageContainer lang() {
        return instance().language();
    }

    public LanguageContainer language() {
        return language;
    }

    private void operation(String operation, Runnable task) {
        LOGGER.info(operation);
        task.run();
    }

    private void hackDataFolder() throws Exception {
        var f_dataFolder = JavaPlugin.class.getDeclaredField("dataFolder");
        f_dataFolder.setAccessible(true);
        f_dataFolder.set(this, new File(folder()));

        var f_configFile = JavaPlugin.class.getDeclaredField("configFile");
        f_configFile.setAccessible(true);
        f_configFile.set(this, new File(folder() + "/config.yml"));
    }

    //----[plugin concept]----
    @Override
    public String id() {
        return ProductInfo.CORE_ID;
    }

    @Override
    public String folder() {
        return System.getProperty("user.dir") + "/plugins/starlight";
    }

    @Override
    public String configId() {
        return ProductInfo.CORE_ID;
    }

    @Override
    public Logger logger() {
        return LOGGER;
    }

    //stages
    private void initializePluginEnv() {
        LOGGER.info("Plugin Environment: ");

        APIProfileTest.test();
        var threadedRegions = APIProfileTest.isFoliaServer();
        var modded = APIProfileTest.isMixedServer();
        this.uuid = UUID.randomUUID().toString();
        this.hasBundler = this.bundledPackageLoader.isPresent();
        PLUGIN = this;
        PluginUtil.CORE_REF.set(this);
        LANGUAGE = this.language().access(ProductInfo.CORE_ID);

        LOGGER.info(" - platform: {}", APIProfileTest.getAPIProfile().toString());
        LOGGER.info(" - region scheduler: {}", threadedRegions);
        LOGGER.info(" - modded environment: {}", modded);
        LOGGER.info(" - instance UUID: {}", this.uuid);
        LOGGER.info(" - qlib environment: {}", PluginPlatform.global());
        LOGGER.info(" - bundler mode: {}", this.hasBundler);

        if (threadedRegions) {
            LOGGER.warn("detected Folia type(Threaded Regions API) environment. using threadedRegions task system.");
        }
        if (modded) {
            LOGGER.warn("detected Arclight type(Forge API) environment. try restart your server rather than /starlight reload.");
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
        this.debug = config.getBoolean("config.plugin.debug");

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

        LOGGER.info(" - core version: {}[{}]", ProductInfo.version(), ProductInfo.API_VERSION);
        LOGGER.info(" - fast boot: {}", this.fastBoot);
        LOGGER.info(" - metrics: {}", metrics);

        if (metrics) {
            this.metrics = new Metrics(this, ProductInfo.BSTATS_ID);
        }
    }

    private void loadFullJar() {
        LOGGER.info("loading full jar...");

        var pluginsPath = System.getProperty("user.dir") + "/plugins/";
        var pluginsDir = new File(pluginsPath);
        var loader = Starlight.class.getClassLoader();

        File jar = null;
        for (File f : Objects.requireNonNull(pluginsDir.listFiles())) {
            if (f.isDirectory() || !f.getName().endsWith(".jar")) {
                continue;
            }

            try {
                if (PluginUtil.getPluginDescription(f).getName().equals(ProductInfo.CORE_ID)) {
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

                if (className.contains("Utils21")) {
                    continue;
                }

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

    @Override
    public void onLoad() {
        try {
            hackDataFolder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //----[plugin]----
    @Override
    public void onEnable() {
        onLoad();

        PLUGIN = this;

        Timer.restartTiming();

        for (String s : ProductInfo.logo(this).split("\n")) {
            Bukkit.getConsoleSender().sendMessage(s);
        }

        BukkitPlatform.init();
        PluginPlatform.global().addLast("starlight:core", new StarlightBukkitPlatform());

        this.initializePluginEnv();
        this.initializeCoreConfiguration();

        BukkitUtil.callEventDirect(new CoreEvent.Launch(this));

        operation("loading libraries...", () -> {
            var repo = getConfig().getString("config.dependency.maven-repo");
            assert repo != null;
            if (!repo.startsWith("http")) {
                repo = MavenRepo.valueOf(repo).getUrl();
            }

            this.libraryManager = new LibraryManager(repo, FilePath.cache());
            this.libraryManager.resolveDependencies(this.metadata.getDependencies());
            this.libraryManager.injectLibraries(this);
        });

        if (!this.fastBoot) {
            this.loadFullJar();
        }

        operation("starting services...", () -> {
            Service.initBase();
            SLInternalPackage.register(PackageManager.INSTANCE.get());
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
        if (!this.initialized) {
            InternalCommands.unregister();
            return;
        }
        this.initialized = false;

        BukkitUtil.callEventDirect(new CoreEvent.Dispose(this));
        Timer.restartTiming();
        LOGGER.info("stopping(v{}-{})...", ProductInfo.version(), ProductInfo.API_VERSION);

        if (this.hasBundler) {
            LOGGER.info("unloading bundled packs...");
            this.bundledPackageLoader.onDisable();
        }

        operation("unloading user services...", () -> ServiceManager.unregisterAll(ServiceLayer.USER));
        operation("unloading framework services...", () -> ServiceManager.unregisterAll(ServiceLayer.FRAMEWORK));
        operation("unloading foundation services...", () -> ServiceManager.unregisterAll(ServiceLayer.FOUNDATION));

        try {
            this.getMetrics().shutdown();
        } catch (Exception e) {
            LOGGER.warn("failed to shutdown metrics: {}", e.getMessage());
            LOGGER.catching(e);
        }

        LOGGER.info("Metrics shutdown successfully");

        LOGGER.info("broadcasting dispose event...");
        TaskService.runFinalizeTask();

        LOGGER.info("broadcasting dispose event...");
        BukkitUtil.callEventDirect(new CoreEvent.PostDispose(this));
        PluginPlatform.global().remove("starlight:core");

        LOGGER.info("done ({} ms)", Timer.passedTime());
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

    public boolean isDebug() {
        return debug;
    }

    public boolean isPluginInitialized() {
        return initialized;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LibraryManager getLibraryManager() {
        return this.libraryManager;
    }

    private static final class StarlightBukkitPlatform extends ForwardingPluginPlatform {
        @Override
        public MinecraftLocale locale(Object sender) {
            return LocaleService.locale(((CommandSender) sender));
        }

        @Override
        public ComponentLike examineComponent(ComponentLike component, Object pointer, MinecraftLocale locale) {
            return TextExaminer.examine(component.asComponent(), locale);
        }

        @Override
        public String globalFormatMessage(String s) {
            return super.globalFormatMessage(PlaceHolderService.format(PlaceHolderService.format(s)));
        }
    }

    public static final class SubPackPluginConceptWrapper implements PluginConcept {
        private final Object handle;

        private SubPackPluginConceptWrapper(Object handle) {
            this.handle = handle;
        }

        public static PluginConcept of(Plugin owner) {
            return new SubPackPluginConceptWrapper(owner);
        }

        @Override
        public String id() {
            return ProductInfo.CORE_ID;
        }

        @Override
        public String folder() {
            return PLUGIN.folder();
        }

        @Override
        public String configId() {
            return "starlight-core";
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
