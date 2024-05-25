package org.tbstcraft.quark.display;

import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.service.network.RemoteMessageService;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.util.function.Consumer;

@QuarkModule(version = "1.1.0")
@ModuleService({ServiceType.EVENT_LISTEN,ServiceType.REMOTE_MESSAGE})
public final class JoinQuitMessage extends PackageModule {

    private void broadcast(String name, Consumer<Player> handler) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p == PlayerUtil.strictFindPlayer(name)) {
                continue;
            }
            handler.accept(p);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        if (this.getConfig().getBoolean("proxy")) {
            RemoteMessageService.getInstance().sendMessage("proxy", "/transfer/join_server", buf -> {
                BufferUtil.writeString(buf, event.getPlayer().getName());
            });
            return;
        }
        String player = event.getPlayer().getName();
        this.broadcast(player, (p) -> this.getLanguage().sendMessageTo(p, "join", player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        if (this.getConfig().getBoolean("proxy")) {
            return;
        }
        String player = event.getPlayer().getName();
        this.broadcast(player, (p) -> this.getLanguage().sendMessageTo(p, "leave", player));
    }

    @RemoteEventHandler("/transfer/join")
    public void onPlayerJoin(RemoteMessageEvent event) {
        String[] data = BufferUtil.readString(event.getData()).split(";");
        this.broadcast(data[0], (p) -> this.getLanguage().sendMessageTo(p, "proxy-join", data[0], data[1]));
        this.getLanguage().sendMessageTo(PlayerUtil.strictFindPlayer(data[0]), "proxy-send", data[2]);
    }

    @RemoteEventHandler("/transfer/leave")
    public void onPlayerQuit(RemoteMessageEvent event) {
        String[] data = BufferUtil.readString(event.getData()).split(";");
        this.broadcast(data[0], (p) -> this.getLanguage().sendMessageTo(p, "proxy-leave", data[0], data[1]));
    }

    @RemoteEventHandler("/transfer/join_proxy")
    public void onPlayerJoinProxy(RemoteMessageEvent event) {
        String data = BufferUtil.readString(event.getData());
        this.broadcast(data, (p) -> this.getLanguage().sendMessageTo(p, "join", data));
        this.getLanguage().sendMessageTo(PlayerUtil.strictFindPlayer(data), "welcome-message", data);
    }

    @RemoteEventHandler("/transfer/quit_proxy")
    public void onPlayerQuitProxy(RemoteMessageEvent event) {
        String data = BufferUtil.readString(event.getData());
        this.broadcast(data, (p) -> this.getLanguage().sendMessageTo(p, "leave", data));
    }
}
