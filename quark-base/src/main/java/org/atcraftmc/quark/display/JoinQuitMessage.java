package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import me.gb2022.modular.module.ApplicationModule;

import java.util.function.Consumer;

@ApplicationModule(version = "1.5.0")
@AutoRegister({Registrations.SERVER_EVENT, Registrations.PLUGIN_VPN_EVENT})
public final class JoinQuitMessage extends SLPackageModule {
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

        if (!ConfigAccessor.getBool(this.config(), "proxy")) {
            var player = event.getPlayer().getName();
            this.broadcast(player, (p) -> MessageAccessor.send(this.language, p, "join", player));
            MessageAccessor.send(this.language, Bukkit.getPlayerExact(player), "welcome-message", player);
        }

        if (ConfigAccessor.getBool(this.config(), "sound")) {
            var volume = this.config().value("volume").floatValue();
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, volume, 1);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        if (ConfigAccessor.getBool(this.config(), "proxy")) {
            return;
        }

        var player = event.getPlayer().getName();
        this.broadcast(player, (p) -> MessageAccessor.send(this.language, p, "leave", player));
    }
}
