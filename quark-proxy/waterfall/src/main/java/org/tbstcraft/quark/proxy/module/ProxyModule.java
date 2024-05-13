package org.tbstcraft.quark.proxy.module;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import org.tbstcraft.quark.proxy.QuarkProxy;
import org.tbstcraft.quark.proxy.RemoteMessage;
import org.tbstcraft.quark.proxy.modulepeer.JoinQuitMessage;

public abstract class ProxyModule implements Listener {
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


    public final void registerEventListener(){
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    public final void registerRemoteMessageListener(){
        RemoteMessage.getMessenger().addMessageHandler(this);
    }
}
