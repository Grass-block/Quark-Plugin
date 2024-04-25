package org.tbstcraft.quark;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.framework.config.Configuration;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.framework.packages.InternalPackages;
import org.tbstcraft.quark.util.Timer;

import java.util.UUID;
import java.util.logging.Logger;

public final class Quark extends JavaPlugin {
    public static final String PLUGIN_ID = "quark";
    public static Language LANGUAGE;
    public static Configuration CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;

    private static boolean coreAvailable = false;

    private String instanceId;

    public static boolean isCoreUnavailable() {
        return coreAvailable;
    }

    @Override
    public void onEnable() {
        PLUGIN = this;

        this.instanceId = UUID.randomUUID().toString();
        try {
            Class.forName("org.tbstcraft.quark.util.Timer");
            Class.forName("org.tbstcraft.quark.Bootstrap");
            Class.forName("org.tbstcraft.quark.Bootstrap$BootOperations");
            Class.forName("org.tbstcraft.quark.Bootstrap$ContextComponent");
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

        InternalPackages.unregisterAll();
        Bootstrap.run(Bootstrap.StopOperations.class, this);

        LOGGER.info("Stop completed.(%d ms)".formatted(Timer.passedTime()));
    }

    public String getInstanceId() {
        return this.instanceId;
    }
}
