package org.atcraftmc.quark.velocity;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.proxy.ProxyServer;
import me.gb2022.apm.remote.RemoteMessenger;
import org.atcraftmc.quark.velocity.command.VelocityCommandManager;
import org.slf4j.Logger;

public abstract class ProxyModule {
    private QuarkVelocity plugin;
    private ProxyServer server;


    public void context(QuarkVelocity plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    public void enable() {

    }

    public void disable() {

    }


    public ProxyServer getServer() {
        return server;
    }

    public QuarkVelocity getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return getPlugin().getLogger();
    }

    public VelocityCommandManager getCommandManager() {
        return getPlugin().getCommandManager();
    }

    public RemoteMessenger getMessenger() {
        return getPlugin().getMessenger();
    }

    public Toml getConfig(String entry) {
        return this.plugin.getConfig().entry(entry);
    }
}
