package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;

import java.nio.charset.StandardCharsets;

public final class ChatSync extends ProxyModule {
    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("quark-pl:msg");

    @Override
    public void enable() {
        getProxy().getChannelRegistrar().register(CHANNEL);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!CHANNEL.equals(event.getIdentifier())) {
            return;
        }

        if (!(event.getSource() instanceof ServerConnection connection)) {
            return;
        }

        var sourceServer = connection.getServer();
        var sourceName = sourceServer.getServerInfo().getName();
        var targetName = this.getGlobalConfig("server").getString(sourceName, sourceName);

        var sender = connection.getPlayer().getUsername();
        var message = GsonComponentSerializer.gson().deserialize(new String(event.getData(), StandardCharsets.UTF_8));
        var template = Config.entry("chat-sync").getString("template").formatted(targetName, sender);
        var combined = TextBuilder.buildComponent(template).append(message);

        getProxy().getAllPlayers().stream().filter((p) -> {
            var server = p.getCurrentServer();

            return server.filter(serverConnection -> !serverConnection.getServer().getServerInfo().equals(sourceServer.getServerInfo()))
                    .isPresent();
        }).forEach((p) -> p.sendMessage(combined));
    }
}
