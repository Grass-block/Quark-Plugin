package org.tbstcraft.quark.proxysupport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.service.proxy.ChannelHandler;
import org.tbstcraft.quark.service.proxy.ProxyChannel;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.service.task.TaskService;

import java.nio.charset.StandardCharsets;
import java.util.*;

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


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = this.getConfig().getInt("accept-delay");
        TaskService.laterTask(delay, () -> {
            this.verifyPlayer(event.getPlayer());
        });

        this.verifyPlayer(event.getPlayer());
    }

    @Override
    public void onMessageReceived(String channelId, byte[] data, ProxyChannel channel) {
        if (Objects.equals(channelId, "quark:player-ip-record.add")) {
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
            buffer.writeBytes(data);

            String name = new String(readByteArray(buffer), StandardCharsets.UTF_8);
            String host = new String(readByteArray(buffer), StandardCharsets.UTF_8);

            this.playerAddressRecords.put(name, host);
            buffer.release();
        }
        if (Objects.equals(channelId, "quark:player-ip-record.remove")) {
            this.playerAddressRecords.remove(new String(data, StandardCharsets.UTF_8));
        }
    }


    private void verifyPlayer(Player player) {
        String address = Objects.requireNonNull(player.getAddress()).getHostName();
        String locale = Language.getLocale(player);
        String name = player.getName();
        if (!this.playerAddressRecords.containsKey(name)) {
            player.kickPlayer(getKickInfo(name, locale));
        }
        if (Objects.equals(this.playerAddressRecords.get(name), address)) {
            return;
        }
        player.kickPlayer(getKickInfo(name, locale));
    }

    public String getKickInfo(String playerName, String locale) {
        String cid = Integer.toString((System.currentTimeMillis() + playerName).hashCode(), 16);
        return this.getLanguage().getMessage(locale, "kick-message", cid);
    }
}
