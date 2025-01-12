package org.atcraftmc.quark_velocity.features;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteQueryEvent;
import me.gb2022.apm.remote.util.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public final class ProxyPing extends ProxyModule {

    @RemoteEventHandler("quark:query/player/ping")
    public void onPingQuery(RemoteQueryEvent event) {
        getProxy().getPlayer(BufferUtil.readString(event.getData())).ifPresentOrElse(
                (player) -> event.writeResult((buffer) -> buffer.writeInt(((int) player.getPing()))),
                () -> event.writeResult((buffer) -> buffer.writeInt(0))
        );
    }
}
