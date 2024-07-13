package org.tbstcraft.quark;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.config.Language;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitPluginManager;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.util.Timer;

import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

public final class Quark extends JavaPlugin {
    public static final String PLUGIN_ID = "quark";
    public static final String CORE_UA = "quark/tm8.5[electron_3];c-api_1.3;pnw_1.4";

    public static Language LANGUAGE;
    public static Configuration CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;
    private static boolean coreAvailable = false;
    private String instanceUUID;
    private boolean fastBoot;

    public static boolean isCoreUnavailable() {
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

        if(APIProfileTest.isArclightBasedServer()){
            task.run();
        }else {
            new Thread(task).start();
        }
    }

    private void detectCounter() {
        final String[] COUNTER_CONFLICT_LIST = new String[]{
                "quark-display",
                //"quark-chat",
                //"quark-security",
                //"quark-utilities"
        };

        Plugin counter = Bukkit.getPluginManager().getPlugin("Counter");

        if (counter == null) {
            return;
        }
        if (!counter.getClass().getName().equals("org.kyoikumi.plugin.counter.Counter")) {
            return;
        }

        LOGGER.severe("detected counter plugin, this may cause conflict.");
        LOGGER.severe("we WON'T fix any problem of duplicated function.");

        for (String s : COUNTER_CONFLICT_LIST) {
            LOGGER.severe("[CounterConflict] rejected local package %s.".formatted(s));
            PackageManager.addRejection(s);
        }
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        BukkitPluginManager.CORE_REF.set(this);

        this.saveDefaultConfig();
        this.reloadConfig();

        this.fastBoot = getConfig().getBoolean("config.startup.fast-boot");
        this.instanceUUID = UUID.randomUUID().toString();

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

        detectCounter();
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
        return fastBoot;
    }
}
