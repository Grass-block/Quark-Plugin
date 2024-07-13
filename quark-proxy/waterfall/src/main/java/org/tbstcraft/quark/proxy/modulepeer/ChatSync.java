package org.tbstcraft.quark.proxy.modulepeer;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import org.tbstcraft.quark.proxy.Config;
import org.tbstcraft.quark.proxy.RemoteMessage;
import org.tbstcraft.quark.proxy.module.ProxyModule;

public final class ChatSync extends ProxyModule {

    @Override
    public void onEnable() {
        this.registerEventListener();
        this.registerRemoteMessageListener();
    }

    @RemoteEventHandler("quark:chat/sync/upstream")
    public void appendChatServerName(RemoteMessageEvent event) {
        String server = event.getServer();

        String target = Config.getSection("server").getString(server, server);

        RemoteMessage.getMessenger().sendBroadcast("quark:chat/sync/downstream", buf -> {
            BufferUtil.writeString(buf, server);
            BufferUtil.writeString(buf, target);
            BufferUtil.writeString(buf, BufferUtil.readString(event.getData()));
            BufferUtil.writeString(buf, BufferUtil.readString(event.getData()));
        });
    }
}
