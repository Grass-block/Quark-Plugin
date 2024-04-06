package org.tbstcraft.quark.service.proxy;

import me.gb2022.commons.crypto.CodecCipher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.tbstcraft.quark.Quark;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public final class ProxyChannel implements PluginMessageListener {
    private final Set<ChannelHandler> handlers = new HashSet<>();

    private final CodecCipher cipher;
    private final Plugin provider;
    private final String id;

    public ProxyChannel(CodecCipher cipher, Plugin provider, String id) {
        this.cipher = cipher;
        this.provider = provider;
        this.id = id;
    }

    public void sendMessage(PluginMessageRecipient recipient, byte[] data) {
        String cid = Base64.getMimeEncoder().encodeToString(this.cipher.encode(this.id.getBytes(StandardCharsets.UTF_8)));
        recipient.sendPluginMessage(this.provider, cid, data);
    }

    public String getId() {
        return this.id;
    }

    public void register() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this.provider, this.id, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this.provider, this.id);
    }

    public void unregister() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this.provider, this.id);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.provider, this.id);
    }

    public void addHandler(ChannelHandler handler) {
        this.handlers.add(handler);
    }

    public void removeHandler(ChannelHandler handler) {
        this.handlers.remove(handler);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (!Objects.equals(channel, this.id)) {
            return;
        }

        byte[] content;
        try {
            content = this.cipher.decode(Base64.getMimeDecoder().decode(data));
        } catch (Exception e) {
            Quark.LOGGER.warning("failed to decode message:" + e.getMessage());
            return;
        }
        for (ChannelHandler handler : this.handlers) {
            handler.onMessageReceived(this.id, content, this);
        }
    }
}
