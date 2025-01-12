package org.atcraftmc.quark.proxysupport;

import me.gb2022.apm.local.ListedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.apm.remote.util.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;
import org.atcraftmc.qlib.texts.placeholder.StringObjectPlaceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@QuarkModule(defaultEnable = false)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ProxyPing extends PackageModule {
    private final Map<String, Integer> ping = new HashMap<>();

    @Override
    public void enable() {
        int interval = getConfig().getInt("interval");
        TaskService.async().timer("quark:proxy-ping:update", interval, interval, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refreshPing(p);
            }
        });
        PlaceHolderService.PLAYER.register("ping", (StringObjectPlaceHolder<Player>) p -> BukkitUtil.formatPing(ping(p)));
        PlaceHolderService.PLAYER.register("ping-value", (StringObjectPlaceHolder<Player>) p -> String.valueOf(ping(p)));
    }

    @Override
    public void disable() {
        TaskService.async().cancel("quark:proxy-ping:update");
        PlaceHolderService.PLAYER.register("ping", (StringObjectPlaceHolder<Player>) p -> BukkitUtil.formatPing(Players.getPing(p)));
        PlaceHolderService.PLAYER.register("ping-value", (StringObjectPlaceHolder<Player>) p -> String.valueOf(Players.getPing(p)));
    }

    @PluginMessageHandler("proxy-ping:update")
    public void onPluginMessage(ListedBroadcastEvent event) {
        refreshPing(event.getArgument(0, Player.class));
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ping(event.getPlayer());
    }

    public int ping(Player player) {
        if (!this.ping.containsKey(player.getName())) {
            return refreshPing(player);
        }
        return this.ping.getOrDefault(player.getName(), 0);
    }

    private int refreshPing(Player player) {
        AtomicInteger ping1 = new AtomicInteger(Players.getPing(player));

        RemoteMessageService.query("proxy", "quark:query/player/ping", (b) -> BufferUtil.writeString(b, player.getName()))
                .timeout(250, () -> getL4jLogger().error("failed to send remote query(%s) for ping!".formatted(player.getName())))
                .result((b) -> ping1.set(b.readInt())).sync();

        int ping = ping1.get();
        this.ping.put(player.getName(), ping);
        return ping;
    }
}
