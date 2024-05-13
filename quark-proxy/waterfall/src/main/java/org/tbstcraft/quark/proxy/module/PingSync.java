package org.tbstcraft.quark.proxy.module;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.event.EventHandler;
import org.tbstcraft.quark.proxy.Config;

import java.util.ArrayList;

public final class PingSync extends ProxyModule {
    private ServerPing cache;
    private long lastUpdate = -1001;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerListener(this.getParent(), this);
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        int freq = Integer.parseInt(String.valueOf(Config.getSection("sync").getLong("motd-update-interval"))) * 1000;
        if (System.currentTimeMillis() - this.lastUpdate > freq) {
            this.update(event.getResponse());
        }
        event.setResponse(this.cache);
    }

    private void update(ServerPing self) {
        String server = Config.getSection("sync").getString("motd-provider");
        ProxyServer.getInstance().getServerInfo(server).ping((result, error) -> {
            if (result == null) {
                return;
            }
            this.cache = result;
        });
        if (this.cache == null) {
            return;
        }
        this.lastUpdate = System.currentTimeMillis();
        ArrayList<ServerPing.PlayerInfo> players = new ArrayList<>();
        for (ServerInfo serverInfo : this.getServer().getServers().values()) {
            for (ProxiedPlayer player : serverInfo.getPlayers()) {
                players.add(new ServerPing.PlayerInfo(player.getName(), player.getUniqueId()));
            }
        }
        if (self != null) {
            this.cache.setVersion(self.getVersion());
        }
        this.cache.getPlayers().setSample(players.toArray(new ServerPing.PlayerInfo[0]));
        this.cache.getPlayers().setOnline(players.size());
    }
}