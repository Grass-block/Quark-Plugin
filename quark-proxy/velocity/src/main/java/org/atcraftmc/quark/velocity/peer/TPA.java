package org.atcraftmc.quark.velocity.peer;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

@AutoRegister(Registers.REMOTE_MESSAGE)
public class TPA extends ProxyModule {
    @RemoteEventHandler("quark:/tpa/forward/request/up")
    public void onRemoteRequestFVD(RemoteMessageEvent event) {
        String sender = BufferUtil.readString(event.getData());
        String receiver = BufferUtil.readString(event.getData());


        var p = getServer().getPlayer(receiver).orElse(null);

        if (p == null) {
            getMessenger().sendMessage(event.getServer(), "quark:/tpa/forward/request/result", (b) -> {
                BufferUtil.writeString(b, "not-found");
                BufferUtil.writeString(b, receiver);
            });

            return;
        }

        var target = p.getCurrentServer().orElseThrow().getServerInfo().getName();

        getMessenger().sendMessage(target, "quark:/tpa/forward/request/down", (b) -> BufferUtil.writeString(b, sender));
    }

    @RemoteEventHandler("\uD83D\uDDFFquark:/tpa/forward/result/up")
    public void onRemoteResultFVD(RemoteMessageEvent event) {
        String sender = BufferUtil.readString(event.getData());
        String receiver = BufferUtil.readString(event.getData());
        String status = BufferUtil.readString(event.getData());

        var p = getServer().getPlayer(receiver).orElse(null);

        if (p == null) {
            return;
        }

        var target = p.getCurrentServer().orElseThrow().getServerInfo().getName();

        getMessenger().sendMessage(target, "quark:/tpa/forward/result/down", (b) -> {
            BufferUtil.writeString(b, sender);
            BufferUtil.writeString(b, receiver);
            BufferUtil.writeString(b, status);
        });
    }

    @RemoteEventHandler("quark:/tpa/toward/upstream")
    public void onRemoteMessageTVD(RemoteMessageEvent event) {



    }
}
