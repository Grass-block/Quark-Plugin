package org.atcraftmc.quark_velocity.features;

import me.gb2022.apm.remote.listen.ChannelListener;
import me.gb2022.apm.remote.listen.MessageChannel;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;

public final class ServerStatementObserver extends ProxyModule implements ChannelListener {

    @Override
    public void enable() {
        getMessenger().messageChannel("server:status").setListener(this);
    }

    @Override
    public void receiveMessage(MessageChannel channel, String pid, String sender, String message) {
        var display = getGlobalConfig("server").getString(sender, sender);
        getProxy().getAllPlayers().stream().filter((p) -> {
            var server = p.getCurrentServer();
            return server.filter(con -> !con.getServer().getServerInfo().getName().equals(sender)).isPresent();
        }).forEach((p) -> Config.language("server-statement-observer").sendMessage(p, message, display));
    }
}
