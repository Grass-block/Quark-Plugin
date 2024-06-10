package org.tbstcraft.quark.proxysupport;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.apm.remote.event.RemoteEventHandler;
import me.gb2022.apm.remote.event.remote.RemoteMessageEvent;
import me.gb2022.apm.remote.protocol.BufferUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@QuarkModule(id = "bungee-connection-protect")
@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.REMOTE_MESSAGE})
public class BungeeConnectionProtect extends PackageModule {
    private final Set<String> sessions = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        long start = System.currentTimeMillis();
        long delay = this.getConfig().getInt("accept-delay");

        while (System.currentTimeMillis() - start < delay) {
            if (this.sessions.contains(name)) {
                return;
            }
            Thread.yield();
        }

        String cid = Integer.toString(Math.abs((System.currentTimeMillis() + name).hashCode()), 16);
        String msg = PluginMessenger.queryKickMessage(name,
                this.getLanguage().getMessage(Locale.SIMPLIFIED_CHINESE, "kick-message", cid), "zh_cn");
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.sessions.remove(event.getPlayer().getName());
    }

    @RemoteEventHandler("/bc-connect/add")
    public void onRemotePlayerJoin(RemoteMessageEvent event) {
        this.sessions.add(BufferUtil.readString(event.getData()));
    }
}
