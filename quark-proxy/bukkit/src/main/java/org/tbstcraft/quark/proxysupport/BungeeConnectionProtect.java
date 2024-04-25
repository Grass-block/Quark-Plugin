package org.tbstcraft.quark.proxysupport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;
import org.tbstcraft.quark.service.proxy.ChannelHandler;
import org.tbstcraft.quark.service.proxy.ProxyChannel;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.service.proxyconnect.ProxyMessageHandler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@EventListener
@QuarkModule(id = "bungee-connection-protect")
public class BungeeConnectionProtect extends PackageModule implements ChannelHandler {
    private final Map<String, String> playerAddressRecords = new HashMap<>();

    private static byte[] readByteArray(ByteBuf buf) {
        int len = buf.readInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return data;
    }

    @Override
    public void enable() {
        ProxyMessageService.addMessageHandler("quark:player-ip-record.add", this);
        ProxyMessageService.addMessageHandler("quark:player-ip-record.remove", this);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(PlayerJoinEvent event) {
        String addr = Objects.requireNonNull(event.getPlayer().getAddress()).getHostName();
        if (Objects.equals(this.playerAddressRecords.get(event.getPlayer().getName()), addr)) {
            return;
        }
        event.getPlayer().kickPlayer(getKickInfo(event.getPlayer().getName(), "zh_cn"));
    }

    @Override
    public void onMessageReceived(String channelId, byte[] data, ProxyChannel channel) {
        if (Objects.equals(channelId, "quark:player-ip-record.add")) {
            System.out.println("???");
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            buffer.writeBytes(data);

            String name = new String(readByteArray(buffer), StandardCharsets.UTF_8);
            String host = new String(readByteArray(buffer), StandardCharsets.UTF_8);

            System.out.println(host);

            this.playerAddressRecords.put(name, host);
            buffer.release();
        }
        if (Objects.equals(channelId, "quark:player-ip-record.remove")) {
            this.playerAddressRecords.remove(new String(data, StandardCharsets.UTF_8));
        }
    }

    @ProxyMessageHandler("quark:player-join-proxy")
    public void onJoinProxy(ByteBuf message) {
        String name = new String(readByteArray(message), StandardCharsets.UTF_8);
        String host = new String(readByteArray(message), StandardCharsets.UTF_8);
        this.playerAddressRecords.put(name, host);
    }

    @ProxyMessageHandler("quark:player-leave-proxy")
    public void onLeaveProxy(ByteBuf message) {
        this.playerAddressRecords.remove(new String(readByteArray(message), StandardCharsets.UTF_8));
    }

    public String getKickInfo(String playerName, String locale) {
        String cid = Integer.toString(Math.abs((System.currentTimeMillis() + playerName).hashCode()), 16);
        return this.getLanguage().getMessage(locale, "kick-message", cid);
    }
}
