package org.tbstcraft.quark.api;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.util.CachedServerIcon;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;

@QuarkEvent
public final class QueryPingEvent extends CustomEvent {
    private CachedServerIcon serverIcon = Bukkit.getServerIcon();
    private String motd = Bukkit.getMotd();
    private int maxPlayers = Bukkit.getMaxPlayers();
    private int onlinePlayers = Bukkit.getOnlinePlayers().size();

    @SuppressWarnings({"unused"})
    public static HandlerList getHandlerList() {
        return getHandlerList(QueryPingEvent.class);
    }

    public CachedServerIcon getServerIcon() {
        return serverIcon;
    }

    public void setServerIcon(CachedServerIcon icon) {
        this.serverIcon = icon;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }
}
