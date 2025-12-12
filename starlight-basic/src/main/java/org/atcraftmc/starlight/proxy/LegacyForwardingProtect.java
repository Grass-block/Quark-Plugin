package org.atcraftmc.starlight.proxy;

import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.apm.remote.event.channel.ChannelListener;
import me.gb2022.apm.remote.event.channel.MessageChannel;
import me.gb2022.apm.remote.event.message.RemoteMessageEvent;
import me.gb2022.commons.math.SHA;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.RemoteMessageService;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;

@ApplicationModule(id = "legacy-forwarding-protect", defaultEnable = false)
@AutoRegister({Registrations.SERVER_EVENT})
public final class LegacyForwardingProtect extends SLPackageModule {
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
        var delay = ConfigAccessor.getInt(this.config(), "accept-delay");

        while (System.currentTimeMillis() - start < delay) {
            if (this.sessions.contains(SHA.getSHA256(name, true))) {
                return;
            }
            Thread.yield();
        }

        this.handle().getLogger().info("{}({}) failed bungee/velocity forwarding check!", name, event.getAddress());

        var cid = Integer.toString(Math.abs((System.currentTimeMillis() + name).hashCode()), 16);
        var kick = MessageAccessor.getMessage(this.language(), MinecraftLocale.ZH_CN, "kick-message", cid);
        var msg = PluginMessenger.queryKickMessage(name, kick, "zh_cn");

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.sessions.remove(event.getPlayer().getName());
    }
}
