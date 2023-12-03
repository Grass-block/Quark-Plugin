package org.tbstcraft.quark;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.command.InternalCommands;
import org.tbstcraft.quark.config.ConfigFile;
import org.tbstcraft.quark.config.LanguageFile;
import org.tbstcraft.quark.module.ModuleManager;
import org.tbstcraft.quark.service.Service;

import java.util.logging.Logger;

//todo /quark about|check
//todo: ui=layout(config)+text(language)
public final class Quark extends JavaPlugin {
    public static LanguageFile LANGUAGE;
    public static ConfigFile CONFIG;
    public static Quark PLUGIN;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        PLUGIN = this;
        LOGGER = this.getLogger();
        LANGUAGE = new LanguageFile("quark_core");
        CONFIG = new ConfigFile("quark_core");
        LOGGER.info("configuration loaded.");
        Service.init();
        SharedObjects.loadGlobalVars();
        ModuleManager.loadStatus();
        LOGGER.info("service initialized.");
        InternalCommands.register();
        LOGGER.info("commands registered.");
        LOGGER.info("core started.");
    }

    @Override
    public void onDisable() {
        ModuleManager.shutdown();
        LOGGER.info("all modules uninstalled.");
        ModuleManager.storeStatus();
        Service.stop();
        LOGGER.info("service stopped.");
        InternalCommands.unregister();
        LOGGER.info("commands unregistered.");
        TaskManager.stopAll();
        LOGGER.info("all tasks stopped.");

        PLUGIN = null;
        getLogger().info("core stopped.");
    }
}
