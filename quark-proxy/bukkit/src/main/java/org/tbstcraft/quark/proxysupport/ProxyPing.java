package org.tbstcraft.quark.proxysupport;

import me.gb2022.apm.local.ListedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.placeholder.StringObjectPlaceHolder;

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
        TaskService.asyncTimerTask("quark:proxy-ping:update", interval, interval, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refreshPing(p);
            }
        });
        PlaceHolderService.PLAYER.register("ping", (StringObjectPlaceHolder<Player>) p -> BukkitUtil.formatPing(ping(p)));
    }

    @Override
    public void disable() {
        TaskService.cancelTask("quark:proxy-ping:update");
        PlaceHolderService.PLAYER.register("ping", (StringObjectPlaceHolder<Player>) p -> BukkitUtil.formatPing(PlayerUtil.getPing(p)));
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
        AtomicInteger ping1 = new AtomicInteger(PlayerUtil.getPing(player));

        RemoteMessageService.query("proxy", "quark:query/player/ping", (b) -> BufferUtil.writeString(b, player.getName()))
                .timeout(250, () -> getLogger().severe("failed to send remote query(%s) for ping!".formatted(player.getName())))
                .result((b) -> ping1.set(b.readInt())).sync();

        int ping = ping1.get();
        this.ping.put(player.getName(), ping);
        return ping;
    }
}
