package org.atcraftmc.quark.velocity.peer;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageExchangeEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public final class ServerStatementObserver extends ProxyModule {
    @RemoteEventHandler("/quark/observe/online")
    public void onServerOnline(RemoteMessageExchangeEvent event) {
        event.writeResult((b) -> {
            String sid = BufferUtil.readString(event.getData());
            String server = this.getConfig("server").getString(sid, sid);
            b.clear();
            BufferUtil.writeString(b, server);
        });
    }

    @RemoteEventHandler("/quark/observe/offline")
    public void onServerOffline(RemoteMessageExchangeEvent event) {
        event.writeResult((b) -> {
            String sid = BufferUtil.readString(event.getData());
            String server = this.getConfig("server").getString(sid, sid);
            b.clear();
            BufferUtil.writeString(b, server);
        });
    }
}
