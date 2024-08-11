package org.atcraftmc.quark.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.gb2022.apm.remote.RemoteMessenger;
import org.atcraftmc.quark.velocity.command.VelocityCommandManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "quark-velocity", version = "1.0", authors = "GrassBlock2022", description = "quark-plugin velocity peer.")
public final class QuarkVelocity {
    private final VelocityCommandManager commandManager = new VelocityCommandManager(this);
    private final ProxyModuleRegManager regManager = new ProxyModuleRegManager(this);
    private final APMRemoteMessenger messenger = new APMRemoteMessenger(this);
    private final ModuleManager moduleManager = new ModuleManager(this);
    private final Config config = new Config(this);

    private final Logger logger;
    private final ProxyServer server;

    @Inject
    public QuarkVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        this.config.load();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.regManager.deferredInit();
        this.messenger.init();
        this.moduleManager.enable();
        Runtime.getRuntime().addShutdownHook(new Thread(this::onServerStop));
    }

    private void onServerStop() {
        this.messenger.stop();
        this.moduleManager.disable();
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public ProxyModuleRegManager getRegManager() {
        return regManager;
    }

    public VelocityCommandManager getCommandManager() {
        return commandManager;
    }

    public RemoteMessenger getMessenger() {
        return messenger.getConnector();
    }

    public Path getDataDirectory() {
        return Path.of(System.getProperty("user.dir") + "/plugins/quark-proxy");
    }

    public Config getConfig() {
        return config;
    }
}
