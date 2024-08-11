package org.tbstcraft.quark.display;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.util.function.Consumer;

@QuarkModule(version = "1.1.0")
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

        if (this.getConfig().getBoolean("proxy")) {
            if (this.getConfig().getBoolean("sound")) {
                var volume = this.getConfig().getDouble("volume");
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, (float) volume, 1);
            }

            RemoteMessageService.message("proxy", "/transfer/join_server", buf -> BufferUtil.writeString(buf, event.getPlayer().getName()));
            return;
        }
        String player = event.getPlayer().getName();
        this.broadcast(player, (p) -> this.language.sendMessage(p, "join", player));
        this.language.sendMessage(Bukkit.getPlayerExact(player), "welcome-message", player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        if (this.getConfig().getBoolean("proxy")) {
            return;
        }
        String player = event.getPlayer().getName();
        this.broadcast(player, (p) -> this.language.sendMessage(p, "leave", player));
    }

    @RemoteEventHandler("/transfer/join")
    public void onPlayerJoin(RemoteMessageEvent event) {
        String[] data = BufferUtil.readString(event.getData()).split(";");
        this.broadcast(data[0], (p) -> this.language.sendMessage(p, "proxy-join", data[0], data[1]));
        this.language.sendMessage(Bukkit.getPlayerExact(data[0]), "proxy-send", data[2]);
    }

    @RemoteEventHandler("/transfer/leave")
    public void onPlayerQuit(RemoteMessageEvent event) {
        String[] data = BufferUtil.readString(event.getData()).split(";");
        this.broadcast(data[0], (p) -> this.language.sendMessage(p, "proxy-leave", data[0], data[1]));
    }

    @RemoteEventHandler("/transfer/join_proxy")
    public void onPlayerJoinProxy(RemoteMessageEvent event) {
        String data = BufferUtil.readString(event.getData());
        this.broadcast(data, (p) -> this.language.sendMessage(p, "join", data));
        this.language.sendMessage(Bukkit.getPlayerExact(data), "welcome-message", data);
    }

    @RemoteEventHandler("/transfer/quit_proxy")
    public void onPlayerQuitProxy(RemoteMessageEvent event) {
        String data = BufferUtil.readString(event.getData());
        this.broadcast(data, (p) -> this.language.sendMessage(p, "leave", data));
    }
}
