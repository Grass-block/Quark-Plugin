package org.tbstcraft.quark.proxy;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.Properties;

public abstract class Sync implements Listener {
    private QuarkProxy parent;
    private ProxyServer server;

    public void init(QuarkProxy parent, ProxyServer server) {
        this.parent = parent;
        this.server = server;
    }

    public void onEnable() {
    }

    public QuarkProxy getParent() {
        return parent;
    }

    public ProxyServer getServer() {
        return server;
    }

    protected Properties getConfig() {
        return getParent().getConfig();
    }
}
