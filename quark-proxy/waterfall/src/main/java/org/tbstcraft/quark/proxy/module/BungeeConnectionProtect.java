package org.tbstcraft.quark.proxy.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import me.gb2022.apm.remote.protocol.BufferUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.RemoteMessage;

import java.nio.charset.StandardCharsets;

public final class BungeeConnectionProtect extends ProxyModule {

    private static void writeBytes(ByteBuf buf, byte[] arr) {
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    private byte[] getVerificationData(ProxiedPlayer player) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();

        writeBytes(buffer, player.getName().getBytes(StandardCharsets.UTF_8));
        writeBytes(buffer, player.getAddress().getHostName().getBytes(StandardCharsets.UTF_8));

        byte[] data = new byte[buffer.writerIndex()];
        buffer.readBytes(data);
        buffer.release();
        return data;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        RemoteMessage.getMessenger().sendMessage(event.getTarget().getName(), "/bc-connect/add", (buffer) -> {
            BufferUtil.writeString(buffer, event.getPlayer().getName());
        });
    }

    @Deprecated(forRemoval = true) //support legacy protocol
    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        String name = event.getPlayer().getName();
        byte[] data = name.getBytes(StandardCharsets.UTF_8);

        //ServerMessageService.send(event.getTarget(), "quark:bc.player-add", data);
    }
}
