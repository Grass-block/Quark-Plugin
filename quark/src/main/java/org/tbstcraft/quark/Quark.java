package org.tbstcraft.quark;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitPluginManager;
import org.tbstcraft.quark.metrics.Metrics;
import org.tbstcraft.quark.util.Timer;

import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * <h3>江城子·程序员之歌</h3>
 * 十年生死两茫茫，写程序，到天亮。千万代码，Bug何处藏。纵使上线又何妨，朝令改，夕断肠。<br>
 * 甲方每天新想法，天天改，日日忙。相顾无言，惟有泪千行。每晚灯火阑珊处，夜难寐，赶工狂。
 */
public final class Quark extends JavaPlugin {
    public static final int METRIC_PLUGIN_ID = 22683;
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm8.6[electron3.1]";

    public static final ILanguageAccess LANGUAGE = LanguageContainer.getInstance().access("quark-core");
    public static Metrics METRICS;
    public static Configuration CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;

    private static boolean coreAvailable = false;
    private final String instanceUUID = UUID.randomUUID().toString();
    private boolean fastBoot;

    public static boolean isCoreAvailable() {
        return coreAvailable;
    }

    public static void reload(CommandSender audience) {
        Runnable task = () -> {
            try {
                Locale locale = org.tbstcraft.quark.data.language.Language.locale(audience);
                String msg = LANGUAGE.getMessage(locale, "packages", "load");

                Class<?> commandManager = Class.forName("org.tbstcraft.quark.foundation.command.CommandManager");
                Class<?> packageManager = Class.forName("org.tbstcraft.quark.framework.packages.PackageManager");
                Class<?> pluginLoader = Class.forName("org.tbstcraft.quark.foundation.platform.BukkitPluginManager");

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

    @Override
    public void onEnable() {
        PLUGIN = this;
        BukkitPluginManager.CORE_REF.set(this);

        this.saveDefaultConfig();
        this.reloadConfig();

        this.fastBoot = getConfig().getBoolean("config.startup.fast-boot");

        METRICS = new Metrics(this, METRIC_PLUGIN_ID);

        try {
            Class.forName("org.tbstcraft.quark.util.Timer");
            Class.forName("org.tbstcraft.quark.Bootstrap");
            Class.forName("org.tbstcraft.quark.Bootstrap$BootOperations");
            Class.forName("org.tbstcraft.quark.Bootstrap$ContextComponent");
            Class.forName("org.tbstcraft.quark.data.config.Queries");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Timer.restartTiming();

        Bootstrap.run(Bootstrap.BootOperations.class, this);
        coreAvailable = true;

        LOGGER.info("Initialization completed.(%d ms)".formatted(Timer.passedTime()));
    }

    @Override
    public void onDisable() {
        Timer.restartTiming();
        coreAvailable = false;

        Bootstrap.run(Bootstrap.StopOperations.class, this);

        LOGGER.info("Stop completed.(%d ms)".formatted(Timer.passedTime()));
    }

    public String getInstanceUUID() {
        return this.instanceUUID;
    }

    public boolean isFastBoot() {
        return this.fastBoot;
    }
}
