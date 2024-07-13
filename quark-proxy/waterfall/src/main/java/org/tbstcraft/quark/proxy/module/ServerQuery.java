package org.tbstcraft.quark.proxy.module;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteQueryEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class ServerQuery extends ProxyModule {

    @Override
    public void onEnable() {
        this.registerRemoteMessageListener();
    }

    @RemoteEventHandler("quark:query/player/ping")
    public void onPingQuery(RemoteQueryEvent event) {
        ProxiedPlayer player = getServer().getPlayer(BufferUtil.readString(event.getData()));
        event.writeResult((buffer) -> buffer.writeInt(player == null ? 0 : player.getPing()));
    }
}
