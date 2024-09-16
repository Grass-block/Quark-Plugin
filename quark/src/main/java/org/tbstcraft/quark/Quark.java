package org.tbstcraft.quark;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.backend.MessageBackend;
import me.gb2022.commons.Timer;
import org.atcraftmc.qlib.command.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.data.config.ConfigAccess;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.foundation.command.QuarkCommandManager;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.foundation.text.TextSender;
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
import java.util.logging.Logger;

/**
 * <h3>江城子·腐竹之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万插件，Bug何处藏。纵使上线又何妨，朝令改，夕断肠。<br>
 * 玩家每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Quark extends JavaPlugin {
    public static final int API_VERSION = 37;
    public static final int BSTATS_ID = 22683;
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm8.77[electron3.5]";

    public static final ILanguageAccess LANGUAGE = LanguageContainer.getInstance().access("quark-core");
    public static final ConfigAccess CONFIG = ConfigContainer.getInstance().access("quark-core");
    public static Quark PLUGIN;
    public static Logger LOGGER;

    private final BundledPackageLoader bundledPackageLoader = new BundledPackageLoader();
    private final CommandManager commandManager = new QuarkCommandManager(this);

    private String uuid;
    private Metrics metrics;
    private boolean fastBoot;
    private boolean initialized;
    private boolean hasBundler = false;

    public static void reload(CommandSender audience) {
        Runnable task = () -> {
            try {
                Locale locale = LocaleService.locale(audience);
                String msg = LANGUAGE.getMessage(locale, "packages", "load");

                Class<?> commandManager = Class.forName("org.atcraftmc.qlib.command.LegacyCommandManager");
                Class<?> packageManager = Class.forName("org.tbstcraft.quark.framework.packages.PackageManager");
                Class<?> pluginLoader = Class.forName("org.tbstcraft.quark.foundation.platform.PluginUtil");

                pluginLoader.getMethod("reload", String.class).invoke(null, PLUGIN_ID);
                packageManager.getMethod("reload").invoke(null);
                commandManager.getMethod("sync").invoke(null);

                audience.sendMessage(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        /* test - remove async reloading.
        if (APIProfileTest.isArclightBasedServer()) {
            task.run();
        } else {
            new Thread(task).start();
        }
         */

        task.run();
    }

    public static Quark getInstance() {
        return (Quark) Bukkit.getPluginManager().getPlugin("quark");
    }

    //----[logging]----
    private void log(String msg, Object... format) {
        this.getLogger().info(msg.formatted(format));
    }

    private void info(String msg, String zh, Object... format) {
        log(Locale.getDefault().getCountry().contains("CN") ? zh : msg, format);
    }

    private void warn(String msg, String zh, Object... format) {
        log(Locale.getDefault().getCountry().equalsIgnoreCase("zh") ? zh : msg, format);
    }

    private void operation(String operation, String zh, Runnable task) {
        info(operation, zh);
        task.run();
    }

    private void operation(String operation, String zh, boolean condition, Runnable task) {
        if (!condition) {
            return;
        }
        operation(operation, zh, task);
    }


    //----[lifecycle]----
    @Override
    public void onEnable() {
        Timer.restartTiming();

        for (String s : ProductInfo.logo(this).split("\n")) {
            Bukkit.getConsoleSender().sendMessage(s);
        }

        info("starting(v%s@API%s)...", "正在启动(v%s@API%s)...", ProductInfo.version(), API_VERSION);


        this.bundledPackageLoader.init();
        info("found bundler packages, constructing....", "找到绑定包，正在构造...");
        this.hasBundler = this.bundledPackageLoader.isPresent();

        operation("loading bootstrap classes...", "加载启动类...", () -> {
            try {
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
        operation("loading plugin context...", "初始化插件上下文...", () -> {
            this.uuid = UUID.randomUUID().toString();

            LOGGER = getLogger();
            PLUGIN = this;

            PluginUtil.CORE_REF.set(this);
            APIProfileTest.test();

            log("platform: %s".formatted(APIProfileTest.getAPIProfile().toString()));

            TextSender.initContext();
        });
        operation("checking environment...", "检查环境...", () -> {
            var folia = APIProfileTest.isFoliaServer();
            var arclight = APIProfileTest.isArclightBasedServer();

            if (folia) {
                warn(
                        "detected Folia type(Threaded Regions API) environment. using folia task system.",
                        "检测到类Folia环境(线程化), 已启用Folia任务系统。"
                    );
            }
            if (arclight) {
                warn(
                        "detected Arclight type(Forge API) environment. try restart your server rather than /quark reload.",
                        "检测到类Arclight环境(Forge混合端), 请不要使用/quark reload重载插件。"
                    );
            }
            if (isFastBoot() && arclight) {
                warn(
                        "fastboot are not available on Arclight type(Forge API) platform! disabling fast-boot.",
                        "快速启动在类Arclight平台(Forge混合端)不可用! 正在关闭快速启动..."
                    );
                this.fastBoot = false;
            }
            if (isFastBoot()) {
                warn(
                        "using Fast-Boot environment, hot-reload may not function well. RESTART your server if any error occurred.",
                        "正在使用快速启动, 热重载可能表现不正常。如果遇到任何错误请重启服务器。"
                    );
            }
        });
        operation("loading core configuration...", "加载核心配置...", () -> {
            this.saveDefaultConfig();
            this.reloadConfig();
            try {
                ProductInfo.METADATA.load(getClass().getResourceAsStream("/product-info.properties"));
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

            operation(
                    "initializing metrics...",
                    "初始化插件数据统计(Metrics)...",
                    metrics,
                    () -> this.metrics = new Metrics(this, BSTATS_ID)
                     );
        });
        operation("loading full jar...", "加载全Jar...", !this.fastBoot, () -> {
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
        operation("starting services...", "启动服务组件...", () -> {
            Service.initBase();
            QuarkInternalPackage.register(PackageManager.INSTANCE.get());

            ClientMessenger.setBackend(MessageBackend.bukkit(Quark.getInstance()));
            ClientMessenger.getBackend().start();
        });

        if (this.hasBundler) {
            info("loading bundled packs...", "检测到绑定存在，正在加载...");
            this.bundledPackageLoader.register();
        }

        info("done. (%d ms)", "完成! (%d ms)", Timer.passedTime());
        this.initialized = true;
    }

    @Override
    public void onDisable() {
        Timer.restartTiming();
        this.initialized = false;
        info("stopping(v%s@API%s)...", "正在停止(v%s@API%s)...", ProductInfo.version(), API_VERSION);

        if (this.hasBundler) {
            info("unloading bundled packs...", "检测到绑定存在，正在卸载...");
            this.bundledPackageLoader.unregister();
        }

        operation("stopping services...", "停止服务组件...", () -> {
            ServiceManager.unregisterAll();
            Service.stopBase();
            ClientMessenger.getBackend().stop();
        });
        operation("destroying context...", "销毁插件上下文...", () -> {
            try {
                this.getMetrics().shutdown();
            } catch (Exception ignored) {
            }

            this.metrics = null;
        });
        operation("running finalize tasks...", "运行卸载任务...", TaskService::runFinalizeTask);

        info("done (%d ms)", "完成! (%d ms)", Timer.passedTime());
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

    public boolean isInitialized() {
        return initialized;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public @NotNull File getFile() {
        return super.getFile();
    }
}
