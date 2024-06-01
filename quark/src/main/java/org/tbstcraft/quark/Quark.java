package org.tbstcraft.quark;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.framework.data.config.Configuration;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.data.config.Queries;
import org.tbstcraft.quark.framework.data.config.YamlUtil;
import org.tbstcraft.quark.util.Timer;
import org.tbstcraft.quark.util.platform.BukkitPluginManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
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
    private boolean fastBoot;

    public static boolean isCoreUnavailable() {
        return coreAvailable;
    }

    @Override
    public void onEnable() {
        PLUGIN = this;
        BukkitPluginManager.INSTANCE_CACHE.set(this);

        this.saveDefaultConfig();
        this.reloadConfig();

        this.fastBoot = getConfig().getBoolean("config.startup.fast-boot");
        this.instanceId = UUID.randomUUID().toString();

        try {
            Class.forName("org.tbstcraft.quark.util.Timer");
            Class.forName("org.tbstcraft.quark.Bootstrap");
            Class.forName("org.tbstcraft.quark.Bootstrap$BootOperations");
            Class.forName("org.tbstcraft.quark.Bootstrap$ContextComponent");
            Class.forName("org.tbstcraft.quark.framework.data.config.Queries");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Timer.restartTiming();

        InputStream templateResource = Objects.requireNonNull(this.getClass().getResourceAsStream("/config.yml"));
        YamlConfiguration template = YamlConfiguration.loadConfiguration(new InputStreamReader(templateResource));

        YamlUtil.update(getConfig(), template, false, 3);

        this.saveConfig();
        Queries.setEnvironmentVars(Objects.requireNonNull(getConfig().getConfigurationSection("config.environment")));

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

    public String getInstanceId() {
        return this.instanceId;
    }

    public boolean isFastBoot() {
        return fastBoot;
    }
}
