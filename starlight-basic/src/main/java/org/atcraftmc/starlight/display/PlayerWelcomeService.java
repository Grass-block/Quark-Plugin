package org.atcraftmc.starlight.display;

import org.atcraftmc.starlight.api.PlayerFirstJoinEvent;
import org.atcraftmc.starlight.core.data.flex.TableColumn;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@SLService(id = "player-welcome")
public interface PlayerWelcomeService extends Service {
    EventListener EVENT_LISTENER = new EventListener();

    @ServiceInject
    static void start() {
        BukkitUtil.registerEventListener(EVENT_LISTENER);
    }

    @ServiceInject
    static void stop() {
        BukkitUtil.unregisterEventListener(EVENT_LISTENER);
    }

    final class EventListener implements Listener {
        public static final TableColumn<Boolean> JOINED = TableColumn.bool("joined", false);

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            if (!JOINED.get(PlayerDataService.PLAYER_LOCAL, event.getPlayer().getUniqueId())) {
                BukkitUtil.callEvent(new PlayerFirstJoinEvent(event.getPlayer()));
                JOINED.set(PlayerDataService.PLAYER_LOCAL, event.getPlayer().getUniqueId(), true);
            }
        }
    }
}
