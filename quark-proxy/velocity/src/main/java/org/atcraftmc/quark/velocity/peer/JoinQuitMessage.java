package org.atcraftmc.quark.velocity.peer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

import java.util.HashMap;
import java.util.Map;

@AutoRegister({Registers.REMOTE_MESSAGE, Registers.VELOCITY_EVENT})
public final class JoinQuitMessage extends ProxyModule {
    private final Map<String, String> previousLocations = new HashMap<>();
    private final Map<String, String> currentLocations = new HashMap<>();

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        String player = event.getPlayer().getUsername();
        String current = event.getServer().getServerInfo().getName();

        if (event.getPreviousServer().isEmpty()) {
            getMessenger().sendMessage(current, "/transfer/join_proxy", buf -> BufferUtil.writeString(buf, player));
            return;
        }

        var prev = event.getPreviousServer().get().getServerInfo().getName();

        getMessenger().sendMessage(current, "/transfer/join", buf -> {
            String server = this.getConfig("server").getString(prev, prev);
            String target = this.getConfig("server").getString(current, current);
            BufferUtil.writeString(buf, "%s;%s;%s".formatted(player, server, target));
        });
        getMessenger().sendMessage(prev, "/transfer/leave", buf -> {
            String server = this.getConfig("server").getString(current, current);
            BufferUtil.writeString(buf, "%s;%s".formatted(player, server));
        });
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        String name = event.getPlayer().getUsername();

        String latest = event.getPlayer()
                .getCurrentServer()
                .orElseThrow(() -> new RuntimeException("no connection!"))
                .getServerInfo()
                .getName();
        getMessenger().sendMessage(latest, "/transfer/quit_proxy", buf -> BufferUtil.writeString(buf, name));
    }
}
