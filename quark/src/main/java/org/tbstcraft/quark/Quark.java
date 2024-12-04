package org.tbstcraft.quark;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.backend.MessageBackend;
import me.gb2022.commons.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.CommandManager;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.api.CoreEvent;
import org.tbstcraft.quark.data.config.ConfigAccess;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.foundation.command.QuarkCommandManager;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.metrics.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarFile;

/**
 * <h3>江城子·腐竹之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万插件，Bug何处藏。纵使上线又何妨，朝令改，夕断肠。<br>
 * 玩家每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Quark extends JavaPlugin {
    public static final Logger LOGGER = LogManager.getLogger("Quark");
    public static final int API_VERSION = 38;
    public static final int BSTATS_ID = 22683;
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm8.8";

    public static final ILanguageAccess LANGUAGE = LanguageContainer.getInstance().access("quark-core");
    public static final ConfigAccess CONFIG = ConfigContainer.getInstance().access("quark-core");
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


    //----[plugin]----
    @Override
    public void onEnable() {
        Timer.restartTiming();

        for (String s : ProductInfo.logo(this).split("\n")) {
            Bukkit.getConsoleSender().sendMessage(s);
        }

        LOGGER.info("starting(v{}-{})...", ProductInfo.version(), API_VERSION);

        this.hasBundler = this.bundledPackageLoader.isPresent();

        operation("loading bootstrap classes...", () -> {
            try {
                Class.forName("net.kyori.adventure.text.ComponentLike");
                Class.forName("me.gb2022.commons.Timer");
                Class.forName("org.tbstcraft.quark.data.config.Queries");
                Class.forName("org.tbstcraft.quark.foundation.platform.APIProfile");
                Class.forName("org.tbstcraft.quark.foundation.text.TextSender");
                Class.forName("org.tbstcraft.quark.foundation.text.TextBuilder");
                Class.forName("org.tbstcraft.quark.foundation.platform.PluginUtil");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        BukkitUtil.callEventDirect(new CoreEvent.Launch(this));

        APIProfileTest.test();
        LOGGER.info("platform: {}", APIProfileTest.getAPIProfile().toString());


        operation("initializing plugin context...", () -> {
            this.uuid = UUID.randomUUID().toString();

            PLUGIN = this;

            PluginUtil.CORE_REF.set(this);
        });
        operation("checking environment...", () -> {
            var folia = APIProfileTest.isFoliaServer();
            var arclight = APIProfileTest.isArclightBasedServer();

            if (folia) {
                LOGGER.warn("detected Folia type(Threaded Regions API) environment. using folia task system.");
            }
            if (arclight) {
                LOGGER.warn("detected Arclight type(Forge API) environment. try restart your server rather than /quark reload.");
            }
            if (isFastBoot() && arclight) {
                LOGGER.warn("fastboot are not available on Arclight type(Forge API) platform! disabling fast-boot.");
                this.fastBoot = false;
            }
            if (isFastBoot()) {
                LOGGER.warn("using Fast-Boot environment, hot-reload may not function well. RESTART your server if any error occurred.");
            }
        });
        operation("loading core configuration...", () -> {
            this.saveDefaultConfig();
            this.reloadConfig();
            try {
                ProductInfo.METADATA.load(getClass().getClassLoader().getResourceAsStream("product-info.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var config = getConfig();

            var fastBoot = config.getBoolean("config.plugin.fast-boot");
            var metrics = config.getBoolean("config.plugin.metrics");
            //var debug = config.getBoolean("config.plugin.debug");

            this.fastBoot = fastBoot;

            InputStream templateResource = Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"));
            YamlConfiguration template = YamlConfiguration.loadConfiguration(new InputStreamReader(templateResource));

            YamlUtil.update(config, template, false, 3);

            saveConfig();

            Queries.setEnvironmentVars(Objects.requireNonNull(config.getConfigurationSection("config.environment")));

            operation("initializing metrics...", metrics, () -> this.metrics = new Metrics(this, BSTATS_ID));
        });
        operation("loading full jar...", !this.fastBoot, () -> {
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
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        });
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
        BukkitUtil.callEventDirect(new CoreEvent.Dispose(this));

        Timer.restartTiming();
        this.initialized = false;
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
}
