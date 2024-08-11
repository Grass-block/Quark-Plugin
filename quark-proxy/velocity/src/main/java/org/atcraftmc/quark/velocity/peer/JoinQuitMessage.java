package org.atcraftmc.quark.velocity.peer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark.velocity.ProxyModule;
import org.atcraftmc.quark.velocity.Registers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AutoRegister({Registers.REMOTE_MESSAGE, Registers.VELOCITY_EVENT})
public final class JoinQuitMessage extends ProxyModule {
    private final Map<String, String> previousLocations = new HashMap<>();
    private final Map<String, String> currentLocations = new HashMap<>();

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        String player = event.getPlayer().getUsername();
        String current = event.getServer().getServerInfo().getName();

        if (this.currentLocations.containsKey(player)) {
            this.previousLocations.put(player, this.currentLocations.get(player));
        }
        this.currentLocations.put(player, current);
    }

    @RemoteEventHandler("/transfer/join_server")
    public void onServerConnect(RemoteMessageEvent event) {
        String player = BufferUtil.readString(event.getData());

        if (!this.currentLocations.containsKey(player)) {
            getLogger().warn("cannot detect player current location. fixed it to lobby");
        }

        String current = this.currentLocations.getOrDefault(player, "lobby");
        String prev = this.previousLocations.get(player);

        if (!this.previousLocations.containsKey(player) || Objects.equals(prev, current)) {
            getMessenger().sendMessage(current, "/transfer/join_proxy", buf -> BufferUtil.writeString(buf, player));
            return;
        }


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

        String latest = this.currentLocations.get(name);
        this.previousLocations.remove(name);
        this.currentLocations.remove(name);

        getMessenger().sendMessage(latest, "/transfer/quit_proxy", buf -> BufferUtil.writeString(buf, name));
    }
}
