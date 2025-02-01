package org.atcraftmc.quark_velocity.features;

import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.event.EndpointJoinEvent;
import me.gb2022.apm.remote.event.EndpointLeftEvent;
import me.gb2022.apm.remote.event.RemoteEventListener;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister(Registers.APM_LISTENER)
public final class ServerStatementObserver extends ProxyModule implements RemoteEventListener {

    @Override
    public void endpointJoined(RemoteMessenger messenger, EndpointJoinEvent event) {
        var sender = event.getServer();
        var display = getGlobalConfig("server").getString(sender, sender);
        getProxy().getAllPlayers().stream().filter((p) -> {
            var server = p.getCurrentServer();
            return server.filter(con -> !con.getServer().getServerInfo().getName().equals(sender)).isPresent();
        }).forEach((p) -> Config.language("server-statement-observer").sendMessage(p, "online", display));
    }

    @Override
    public void endpointLeft(RemoteMessenger messenger, EndpointLeftEvent event) {
        var sender = event.getServer();
        var display = getGlobalConfig("server").getString(sender, sender);
        getProxy().getAllPlayers().stream().filter((p) -> {
            var server = p.getCurrentServer();
            return server.filter(con -> !con.getServer().getServerInfo().getName().equals(sender)).isPresent();
        }).forEach((p) -> Config.language("server-statement-observer").sendMessage(p, "offline", display));
    }
}
