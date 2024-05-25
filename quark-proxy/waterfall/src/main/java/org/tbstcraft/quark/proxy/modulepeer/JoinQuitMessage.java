package org.tbstcraft.quark.proxy.modulepeer;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.Config;
import org.tbstcraft.quark.proxy.QuarkProxy;
import org.tbstcraft.quark.proxy.RemoteMessage;
import org.tbstcraft.quark.proxy.module.ProxyModule;

import java.util.HashMap;
import java.util.Map;

public class JoinQuitMessage extends ProxyModule {
    private final Map<String, String> previousLocations = new HashMap<>();
    private final Map<String, String> currentLocations = new HashMap<>();

    @Override
    public void onEnable() {
        this.registerEventListener();
        this.registerRemoteMessageListener();
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        String player = event.getPlayer().getName();
        String current = event.getTarget().getName();

        if (this.currentLocations.containsKey(player)) {
            this.previousLocations.put(player, this.currentLocations.get(player));
        }
        this.currentLocations.put(player, current);
    }


    @RemoteEventHandler("/transfer/join_server")
    public void onServerConnect(RemoteMessageEvent event) {
        String player = BufferUtil.readString(event.getData());


        if (this.currentLocations.containsKey(player)) {
            QuarkProxy.LOGGER.warning("cannot detect player current location. fixed it to lobby");
        }
        String prev = this.previousLocations.get(player);
        String current = this.currentLocations.getOrDefault(player,"lobby");

        if (!this.previousLocations.containsKey(player)) {
            RemoteMessage.getMessenger().sendMessage(current, "/transfer/join_proxy", buf -> BufferUtil.writeString(buf, player));
            return;
        }

        RemoteMessage.getMessenger().sendMessage(current, "/transfer/join", buf -> {
            String server = Config.getSection("server").getString(prev, prev);
            String target = Config.getSection("server").getString(current, current);
            BufferUtil.writeString(buf, "%s;%s;%s".formatted(player, server, target));
        });
        RemoteMessage.getMessenger().sendMessage(prev, "/transfer/leave", buf -> {
            String server = Config.getSection("server").getString(current, current);
            BufferUtil.writeString(buf, "%s;%s".formatted(player, server));
        });
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        String name = event.getPlayer().getName();

        String latest = this.currentLocations.get(name);
        this.previousLocations.remove(name);
        this.currentLocations.remove(name);

        RemoteMessage.getMessenger().sendMessage(latest, "/transfer/quit_proxy", buf -> BufferUtil.writeString(buf, name));
    }
}
