package org.tbstcraft.quark.proxy.module;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageExchangeEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import org.tbstcraft.quark.proxy.Config;

public class ServerStatementObserver extends ProxyModule {

    @Override
    public void onEnable() {
        this.registerRemoteMessageListener();
    }

    @RemoteEventHandler("/quark/observe/online")
    public void onServerOnline(RemoteMessageExchangeEvent event) {
        event.writeResult((b) -> {
            String sid = BufferUtil.readString(event.getData());
            String server = Config.getSection("server").getString(sid, sid);
            b.clear();
            BufferUtil.writeString(b, server);
        });
    }

    @RemoteEventHandler("/quark/observe/offline")
    public void onServerOffline(RemoteMessageExchangeEvent event) {
        event.writeResult((b) -> {
            String sid = BufferUtil.readString(event.getData());
            String server = Config.getSection("server").getString(sid, sid);
            b.clear();
            BufferUtil.writeString(b, server);
        });
    }
}
