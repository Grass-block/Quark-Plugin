package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class TabSync extends ProxyModule {

    @Override
    public void enable() {
        getProxy().getScheduler().buildTask(getPlugin(), () -> {
            for (var server : getProxy().getAllServers()) {
                render(server);
            }
        }).repeat(1, TimeUnit.SECONDS).schedule();
    }


    public void render(RegisteredServer server) {
        var players = this.getProxy()
                .getAllServers()
                .stream()
                .filter((s) -> !Objects.equals(s.getServerInfo(), server.getServerInfo()))
                .flatMap((s) -> s.getPlayersConnected().stream())
                .collect(Collectors.toSet());

        for (var viewer : server.getPlayersConnected()) {
            var tab = viewer.getTabList();

            players.forEach((p) -> p.getCurrentServer()
                    .ifPresent((s) -> this.render(tab, p, s.getServerInfo())));
        }
    }


    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        renderPlayerTo(event.getPlayer(), event.getServer().getServerInfo());
    }

    @Subscribe
    public void onPlayerLogout(DisconnectEvent event) {
        removePlayer(event.getPlayer());
    }

    public void renderPlayerTo(Player player, ServerInfo server) {
        for (var viewer : getProxy().getAllPlayers()) {
            if (Objects.equals(viewer.getUsername(), player.getUsername())) {
                continue;
            }

            if (viewer.getCurrentServer().map((s) -> s.getServerInfo().equals(server)).orElse(false)) {
                continue;
            }

            render(viewer, player, server);
            viewer.getCurrentServer().map(ServerConnection::getServerInfo).ifPresent((s) -> render(player, viewer, s));
        }
    }

    public void render(TabList list, Player player, ServerInfo serverInfo) {
        var entryId = player.getUniqueId();

        var template = getConfig("tab-sync").getString("format");
        var serverId = serverInfo.getName();
        var serverName = getGlobalConfig("server").getString(serverId, serverId);
        var displayName = TextBuilder.buildComponent(template.formatted(serverName, player.getUsername()));

        var entry = list.getEntry(entryId);

        if (entry.isPresent() && Objects.equals(entry.get().getProfile().getId(), entryId)) {
            var e = entry.get();

            e.setDisplayName(displayName);
            e.setLatency((int) player.getPing());
            return;
        }

        var builder = TabListEntry.builder();

        builder.profile(player.getGameProfile());
        builder.displayName(displayName);
        builder.latency((int) player.getPing());
        builder.listed(true);
        builder.tabList(list);

        if (list.containsEntry(entryId)) {
            list.removeEntry(entryId);
        }

        list.addEntry(builder.build());
    }

    public void render(Player viewer, Player data, ServerInfo serverInfo) {
        render(viewer.getTabList(), data, serverInfo);
    }

    public void removePlayer(Player player) {
        getProxy().getAllPlayers().stream().filter((p) -> p != player).forEach((p) -> {
            var uuid = player.getUniqueId();
            p.getTabList().removeEntry(uuid);
        });
    }
}
