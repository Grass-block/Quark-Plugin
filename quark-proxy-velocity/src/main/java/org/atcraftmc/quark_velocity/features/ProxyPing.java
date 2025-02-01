package org.atcraftmc.quark_velocity.features;

import me.gb2022.apm.remote.RemoteMessenger;
import me.gb2022.apm.remote.event.APMRemoteEvent;
import me.gb2022.apm.remote.event.message.RemoteQueryEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public final class ProxyPing extends ProxyModule {

    @APMRemoteEvent("player:ping")
    public void onPingQuery(RemoteMessenger context, RemoteQueryEvent event) {
        getProxy().getPlayer(event.decode(String.class))
                .ifPresentOrElse((player) -> event.write(String.valueOf(player.getPing())), () -> event.write(String.valueOf(0)));
    }
}
