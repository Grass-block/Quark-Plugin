package org.tbstcraft.quark.proxy;

import net.md_5.bungee.api.plugin.Plugin;
import org.tbstcraft.quark.proxy.module.*;
import org.tbstcraft.quark.proxy.modulepeer.ChatSync;
import org.tbstcraft.quark.proxy.modulepeer.JoinQuitMessage;

import java.util.logging.Logger;

public final class QuarkProxy extends Plugin {
    public static final ProxyModule[] MODULES = new ProxyModule[]{
            new PingSync(),
            new KickSync(),
            new BungeeConnectionProtect(),
            new JoinQuitMessage(),
            new ServerStatementObserver(),
            new ChatSync(),
            new ServerQuery()
    };


    public static Logger LOGGER;

    public static QuarkProxy INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = this.getLogger();
        Config.load();

        RemoteMessage.init(Config.getSection("remote"));

        for (ProxyModule s : MODULES) {
            s.init(this, this.getProxy());
            s.onEnable();
        }
        HubCommand.register(this);
    }
}

