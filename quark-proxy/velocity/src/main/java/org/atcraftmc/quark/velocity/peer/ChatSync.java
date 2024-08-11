package org.atcraftmc.quark.velocity.peer;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public final class ChatSync extends ProxyModule {

    @RemoteEventHandler("quark:chat/sync/upstream")
    public void appendChatServerName(RemoteMessageEvent event) {
        String server = event.getServer();

        String target = getConfig("server").getString(server, server);

        getMessenger().sendBroadcast("quark:chat/sync/downstream", buf -> {
            BufferUtil.writeString(buf, server);
            BufferUtil.writeString(buf, target);
            BufferUtil.writeString(buf, BufferUtil.readString(event.getData()));
            BufferUtil.writeString(buf, BufferUtil.readString(event.getData()));
        });
    }
}
