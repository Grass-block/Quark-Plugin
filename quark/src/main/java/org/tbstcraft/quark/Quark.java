package org.tbstcraft.quark;

import me.gb2022.apm.client.ClientMessenger;
import me.gb2022.apm.client.backend.MessageBackend;
import me.gb2022.commons.Timer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.PluginUtil;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.metrics.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * <h3>江城子·程序员之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万代码，Bug何处藏。纵使上线又何妨，朝令改，夕断肠。<br>
 * 用户每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Quark extends JavaPlugin {
    public static final int API_VERSION = 33;
    public static final int METRIC_PLUGIN_ID = 22683;
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm8.7[electron3.3]";

    public static final ILanguageAccess LANGUAGE = LanguageContainer.getInstance().access("quark-core");
    public static Configuration CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;
    public static Metrics METRICS;

    private static boolean coreAvailable = false;
    private final String instanceUUID = UUID.randomUUID().toString();
    private boolean fastBoot;

    public static void reload(CommandSender audience) {
        Runnable task = () -> {
            try {
                Locale locale = org.tbstcraft.quark.data.language.Language.locale(audience);
                String msg = LANGUAGE.getMessage(locale, "packages", "load");

                Class<?> commandManager = Class.forName("org.tbstcraft.quark.foundation.command.CommandManager");
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

        if (APIProfileTest.isArclightBasedServer()) {
            task.run();
        } else {
            new Thread(task).start();
        }
    }

    public static boolean isCoreAvailable() {
        return coreAvailable;
    }

    //----[logging]----
    private void log(String msg, Object... format) {
        this.getLogger().info(msg.formatted(format));
    }

    private void operation(String operation, Runnable task) {
        log(operation);
        task.run();
    }

    private void operation(String operation, boolean condition, Runnable task) {
        if (!condition) {
            return;
        }
        operation(operation, task);
    }


    //----[lifecycle]----
    @Override
    public void onEnable() {
        Timer.restartTiming();

        for (String s : ProductInfo.logo(this).split("\n")) {
            Bukkit.getConsoleSender().sendMessage(s);
        }

        operation("loading bootstrap classes...", () -> {
            try {
                Class.forName("me.gb2022.commons.Timer");
                Class.forName("org.tbstcraft.quark.data.config.Queries");
                Class.forName("org.tbstcraft.quark.foundation.platform.APIProfile");
                Class.forName("org.tbstcraft.quark.foundation.text.TextSender");
                Class.forName("org.tbstcraft.quark.foundation.text.TextBuilder");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        operation("loading plugin context...", () -> {
            PLUGIN = this;
            LOGGER = this.getLogger();

            PluginUtil.CORE_REF.set(this);
            APIProfileTest.test();

            log("platform: %s".formatted(APIProfileTest.getAPIProfile().toString()));

            TextSender.initContext();
        });
        operation("checking environment...", () -> {
            if (APIProfileTest.isFoliaServer()) {
                TextSender.sendToConsole(TextBuilder.build(ChatColor.translateAlternateColorCodes('&', """
                        Quark核心检测到您正在使用Folia服务端!{color(white)}
                          Folia兼容已自动启用。我们不保证任何功能的可用!
                        {color(red)}Quark core detected you are using Folia Server!{color(white)}
                          Folia compat are automatically enabled.We do NOT ensure any feature's availability!
                        """)));
            }
            if (APIProfileTest.isArclightBasedServer()) {
                TextSender.sendToConsole(TextBuilder.build("""
                                                                   {color(red)}Quark核心检测到Arclight/Mohist服务端, 自动更新系统将不可用。若您需要更新，请重启。
                                                                   {color(red)}Quark core detected Arclight/Mohist server, auto-update system will be unavailable. Please RESTART if you want to update!
                                                                   """));
            }

            if (isFastBoot()) {
                if (APIProfileTest.isArclightBasedServer()) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "检测到Mohist/ArcLight平台，无法启用快速启动(FastBoot)");
                    this.fastBoot = false;
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "快速启动(FastBoot)已开启。热重载功能将失效");
                }
            }
        });
        operation("loading core configuration...", () -> {
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
            var debug = config.getBoolean("config.plugin.debug");

            this.fastBoot = fastBoot;

            InputStream templateResource = Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"));
            YamlConfiguration template = YamlConfiguration.loadConfiguration(new InputStreamReader(templateResource));

            YamlUtil.update(config, template, false, 3);

            saveConfig();

            Quark.CONFIG = new Configuration("quark-core");
            Queries.setEnvironmentVars(Objects.requireNonNull(config.getConfigurationSection("config.environment")));

            operation("initializing metrics...", metrics, () -> METRICS = new Metrics(this, METRIC_PLUGIN_ID));
        });
        operation("loading full jar...", !this.fastBoot, () -> {
            try (JarFile jarFile = new JarFile(new File(System.getProperty("user.dir") + "/plugins/quark.jar"))) {
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.getName().endsWith(".class")) {
                        continue;
                    }
                    String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                    if (className.contains("internal")) {
                        continue;
                    }
                    if (className.contains("META-INF")) {
                        continue;
                    }
                    try {
                        Quark.class.getClassLoader().loadClass(className);
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        });
        operation("starting services...", () -> {
            Service.initBase();
            QuarkInternalPackage.register(PackageManager.INSTANCE.get());

            ClientMessenger.setBackend(MessageBackend.bukkit(Quark.PLUGIN));
            ClientMessenger.getBackend().start();
        });

        log("done (%d ms)".formatted(Timer.passedTime()));
        coreAvailable = true;
    }

    @Override
    public void onDisable() {
        Timer.restartTiming();
        coreAvailable = false;

        operation("stopping services...", () -> {
            ServiceManager.unregisterAll();
            Service.stopBase();
            ClientMessenger.getBackend().stop();
        });
        operation("destroying context...", () -> {
            Quark.PLUGIN = null;
            Quark.METRICS = null;
            Quark.LOGGER = null;
        });

        log("done (%d ms)".formatted(Timer.passedTime()));
    }


    //----[properties]----
    public String getInstanceUUID() {
        return this.instanceUUID;
    }

    public boolean isFastBoot() {
        return this.fastBoot;
    }
}
