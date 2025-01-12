package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

@AutoRegister({Registers.VELOCITY_EVENT})
public final class ServerTransferMessage extends ProxyModule {
    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        var lang = Config.language("server-transfer-message");

        var player = event.getPlayer().getUsername();
        var uuid = event.getPlayer().getUniqueId();

        var target = event.getServer();
        var tgt = event.getServer().getServerInfo().getName();
        var targetDisplayName = this.getGlobalConfig("server").getString(tgt, tgt);

        if (event.getPreviousServer().isEmpty()) {
            target.getPlayersConnected()
                    .stream()
                    .filter((p) -> !p.getUniqueId().equals(uuid))
                    .forEach((p) -> lang.sendMessage(p, "join-proxy", player));

            lang.sendMessage(event.getPlayer(), "join-message", player);

            return;
        }

        var previous = event.getPreviousServer().get();
        var prev = event.getPreviousServer().get().getServerInfo().getName();
        var previousDisplayName = this.getGlobalConfig("server").getString(prev, prev);

        target.getPlayersConnected()
                .stream()
                .filter((p) -> !p.getUniqueId().equals(uuid))
                .forEach((p) -> lang.sendMessage(p, "join-server", player, previousDisplayName));

        previous.getPlayersConnected()
                .stream()
                .filter((p) -> !p.getUniqueId().equals(uuid))
                .forEach((p) -> lang.sendMessage(p, "leave-server", player, targetDisplayName));

        lang.sendMessage(event.getPlayer(), "transfer-message", targetDisplayName);
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        var lang = Config.language("server-transfer-message");

        event.getPlayer()
                .getCurrentServer()
                .ifPresent(s -> s.getServer()
                        .getPlayersConnected()
                        .stream()
                        .filter((p) -> !p.equals(event.getPlayer()))
                        .forEach((p) -> lang.sendMessage(p, "leave-proxy", event.getPlayer().getUsername())));
    }
}
