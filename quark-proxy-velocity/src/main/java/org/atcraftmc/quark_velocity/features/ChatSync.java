package org.atcraftmc.quark_velocity.features;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.quark_velocity.Config;
import org.atcraftmc.quark_velocity.ProxyModule;
import org.atcraftmc.quark_velocity.Registers;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@AutoRegister(Registers.VELOCITY_EVENT)
public final class ChatSync extends ProxyModule {
    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("quark_plugin:msg");

    @Override
    public void enable() {
        getProxy().getChannelRegistrar().register(CHANNEL);
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event) {
        if (!CHANNEL.equals(event.getIdentifier())) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection connection)) {
            return;
        }

        var server = connection.getServer();
        var player = connection.getPlayer();
        var message = GsonComponentSerializer.gson().deserialize(new String(event.getData(), StandardCharsets.UTF_8));

        var serverInfo = server.getServerInfo();
        var serverId = serverInfo.getName();
        var targetName = this.getGlobalConfig("server").getString(serverId, serverId);

        var template = Config.entry("chat-sync").getString("template").formatted(targetName, player.getUsername());
        var line = TextBuilder.buildComponent(template).append(message);

        getProxy().getAllPlayers()
                .stream()
                .filter((p) -> !Objects.equals(p, player))
                .filter((p) -> p.getCurrentServer()
                        .map((s) -> !Objects.equals(serverInfo, s.getServerInfo()))
                        .orElse(false))
                .forEach((p) -> p.sendMessage(line));
    }
}
