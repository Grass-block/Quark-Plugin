package org.atcraftmc.quark.proxy;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.apm.remote.event.channel.ChannelListener;
import me.gb2022.apm.remote.event.channel.MessageChannel;
import me.gb2022.apm.remote.event.message.RemoteMessageEvent;
import me.gb2022.commons.math.SHA;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.RemoteMessageService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@QuarkModule(defaultEnable = false)
@AutoRegister({ServiceType.EVENT_LISTEN})
public final class LegacyForwardingProtect extends PackageModule {
    private final Set<String> sessions = new HashSet<>();

    @Override
    public void enable() {
        RemoteMessageService.instance().messageChannel("forwarding:verification").setListener(new ChannelListener() {
            @Override
            public void handle(MessageChannel channel, RemoteMessageEvent event) {
                sessions.add(event.decode(String.class));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        var name = event.getName();
        var start = System.currentTimeMillis();
        var delay = this.getConfig().getInt("accept-delay");

        while (System.currentTimeMillis() - start < delay) {
            if (this.sessions.contains(SHA.getSHA256(name, true))) {
                return;
            }
            Thread.yield();
        }

        getL4jLogger().info("{}({}) failed bungee/velocity forwarding check!", name, event.getAddress());

        var cid = Integer.toString(Math.abs((System.currentTimeMillis() + name).hashCode()), 16);
        var kick = this.getLanguage().getMessage(Locale.SIMPLIFIED_CHINESE, "kick-message", cid);
        var msg = PluginMessenger.queryKickMessage(name, kick, "zh_cn");

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.sessions.remove(event.getPlayer().getName());
    }
}
