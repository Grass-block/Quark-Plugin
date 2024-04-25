package org.tbstcraft.quark.proxy.modulepeer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.ServerMessageService;
import org.tbstcraft.quark.proxy.Sync;

import java.nio.charset.StandardCharsets;

public class BungeeConnectionProtect extends Sync {

    private static void writeBytes(ByteBuf buf, byte[] arr) {
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    @EventHandler
    public void onPlayerJoinProxy(PostLoginEvent event) {
        for (ServerInfo server : getServer().getServersCopy().values()) {
            ServerMessageService.send(server, "quark:player-ip-record.add", getVerificationData(event.getPlayer()));
        }
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
    public void onPlayerLeaveProxy(PlayerDisconnectEvent event) {
        for (ServerInfo server : getServer().getServersCopy().values()) {
            byte[] data = event.getPlayer().getName().getBytes(StandardCharsets.UTF_8);
            ServerMessageService.send(server, "quark:player-ip-record.remove", data);
        }
    }

    @Deprecated
    @EventHandler
    public void onPlayerConnect(ServerConnectEvent event) {
        String name = event.getPlayer().getName();
        byte[] data = name.getBytes(StandardCharsets.UTF_8);

        ServerMessageService.send(event.getTarget(), "quark:bc.player-add", data);


        event.getPlayer().getUUID();

        event.getPlayer().setTabHeader(new TextComponent("114514"), null);
        ServerMessageService.send(event.getTarget(), "quark:player-ip-record.add", getVerificationData(event.getPlayer()));
    }
}
