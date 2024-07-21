package org.tbstcraft.quark.util.container;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.api.DelayedPlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.internal.CacheService;

import java.util.List;

public interface CachedInfo {
    EventAdapter EVENT_ADAPTER = new EventAdapter();

    static void init() {
        BukkitUtil.registerEventListener(EVENT_ADAPTER);
        CacheService.setItem("quark:players", PlayerUtil.getAllPlayerNames());
        refreshPlayerNames();
    }

    static void stop() {
        BukkitUtil.unregisterEventListener(EVENT_ADAPTER);
    }


    static void refreshPlayerNames() {
        CacheService.setItem("quark:online_players", PlayerUtil.getAllOnlinePlayerNames());
    }

    @SuppressWarnings("unchecked")
    static List<String> getOnlinePlayerNames() {
        return CacheService.getItem("quark:online_players", List.class);
    }

    @SuppressWarnings("unchecked")
    static List<String> getAllPlayerNames() {
        CacheService.setItem("quark:players", PlayerUtil.getAllPlayerNames());
        return CacheService.getItem("quark:players", List.class);
    }


    final class EventAdapter implements Listener {
        @EventHandler
        public void onPlayerJoin(DelayedPlayerJoinEvent event) {
            refreshPlayerNames();
        }

        @EventHandler
        public void onPlayerLeave(PlayerLoginEvent event) {
            refreshPlayerNames();
        }
    }
}
