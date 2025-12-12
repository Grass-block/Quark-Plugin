package org.atcraftmc.starlight.proxy;

import me.gb2022.apm.local.ListedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.texts.placeholder.StringObjectPlaceHolder;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationModule(id = "proxy-ping", defaultEnable = false)
@AutoRegister(Registrations.SERVER_EVENT)
public final class ProxyPing extends SLPackageModule {
    private final Map<String, Integer> ping = new HashMap<>();

    @Inject
    private Logger logger;

    @Override
    public void enable() {
        int interval = ConfigAccessor.getInt(config(), "interval");
        TaskService.async().timer("starlight:proxy-ping:update", interval, interval, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                refreshPing(p);
            }
        });
        PlaceHolderService.PLAYER.register("ping", (StringObjectPlaceHolder<Player>) p -> BukkitUtil.formatPing(ping(p)));
        PlaceHolderService.PLAYER.register("ping-value", (StringObjectPlaceHolder<Player>) p -> String.valueOf(ping(p)));
    }

    @Override
    public void disable() {
        TaskService.async().cancel("starlight:proxy-ping:update");
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
        var ping1 = new AtomicInteger(Players.getPing(player));

        RemoteMessageService.instance()
                .query(config().value("query-target").string(), "player:ping", player.getName())
                .timeout(250, () -> this.logger.error("failed to send remote query({}) for ping!", player.getName()))
                .result((b) -> ping1.addAndGet(Integer.parseInt(b)))
                .request();

        int ping = ping1.get();
        this.ping.put(player.getName(), ping);
        return ping;
    }
}
