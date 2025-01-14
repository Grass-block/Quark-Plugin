package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.function.Consumer;

@QuarkModule(version = "1.5.0")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
public final class JoinQuitMessage extends PackageModule {
    @Inject
    private LanguageEntry language;

    private void broadcast(String name, Consumer<Player> handler) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == Bukkit.getPlayerExact(name)) {
                continue;
            }
            handler.accept(p);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        if (!this.getConfig().getBoolean("proxy")) {
            var player = event.getPlayer().getName();
            this.broadcast(player, (p) -> this.language.sendMessage(p, "join", player));
            this.language.sendMessage(Bukkit.getPlayerExact(player), "welcome-message", player);
        }

        if (this.getConfig().getBoolean("sound")) {
            var volume = this.getConfig().getFloat("volume");
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, volume, 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        if (this.getConfig().getBoolean("proxy")) {
            return;
        }

        var player = event.getPlayer().getName();
        this.broadcast(player, (p) -> this.language.sendMessage(p, "leave", player));
    }
}
