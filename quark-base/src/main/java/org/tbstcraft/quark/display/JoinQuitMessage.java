package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

@EventListener
@QuarkModule(version = "1.1.0")
public final class JoinQuitMessage extends PackageModule {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == event.getPlayer()) {
                continue;
            }
            this.getLanguage().sendMessageTo(p, "join", event.getPlayer().getName());
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == event.getPlayer()) {
                continue;
            }
            this.getLanguage().sendMessageTo(p, "leave", event.getPlayer().getName());
        }
        event.setQuitMessage(null);
    }
}
