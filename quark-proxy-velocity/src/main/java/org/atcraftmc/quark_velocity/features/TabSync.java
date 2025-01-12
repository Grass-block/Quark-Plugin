package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

import java.util.concurrent.TimeUnit;

@AutoRegister(Registers.VELOCITY_EVENT)
public class TabSync extends ProxyModule {
    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        updateTabList(event.getPlayer(), true);
    }

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        updateTabList(event.getPlayer(), true);
    }

    @Subscribe
    public void onPlayerLogout(DisconnectEvent event) {
        updateTabList(event.getPlayer(), false);
    }

    // 更新指定玩家的Tab列表，添加所有在线玩家并加上前缀
    private void updateTabList(Player player, boolean join) {
        getProxy().getScheduler().buildTask(getPlugin(), () -> {
            var target = player.getTabList();

            for (var viewer : getProxy().getAllPlayers()) {
                var view = viewer.getTabList();

                if (join) {
                    build(player, view);
                    build(viewer, target);
                } else {
                    view.removeEntry(player.getUniqueId());
                }
            }
        }).delay(1500, TimeUnit.MILLISECONDS);
    }

    public void build(Player player, TabList target) {
        if (target == null) {
            return;
        }

        target.addEntry(target.buildEntry(player.getGameProfile(), getName(player), 1, 0));
    }

    private Component getName(Player player) {
        var p = player.getCurrentServer();
        if (p.isEmpty()) {
            return Component.empty();
        }

        var server = p.get().getServer().getServerInfo().getName();
        var name = getGlobalConfig("server").getString(server, server);

        return TextBuilder.buildComponent(getConfig("tab").getString("format").formatted(name, player.getGameProfile().getName()));
    }
}
