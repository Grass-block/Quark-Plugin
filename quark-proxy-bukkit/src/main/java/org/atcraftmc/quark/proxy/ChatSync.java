package org.atcraftmc.quark.proxy;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.nio.charset.StandardCharsets;

@SLModule(version = "1.0.0")
@AutoRegister({ServiceType.EVENT_LISTEN})
@Components({ChatSync.BukkitListener.class, ChatSync.PaperListener.class})
public final class ChatSync extends PackageModule {
    @Override
    public void enable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(Starlight.instance(), "quark_plugin:msg");
    }

    public void send(Player player, Component message) {
        var payload = GsonComponentSerializer.gson().serialize(message);
        player.sendPluginMessage(Starlight.instance(), "quark_plugin:msg", payload.getBytes(StandardCharsets.UTF_8));
    }

    @AutoRegister({ServiceType.EVENT_LISTEN})
    public static final class BukkitListener extends ModuleComponent<ChatSync> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            try {
                Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
                throw new APIIncompatibleException("assertion failed");
            } catch (ClassNotFoundException ignored) {
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onChat(AsyncPlayerChatEvent event) {
            this.parent.send(event.getPlayer(), Component.text(event.getMessage()));
        }
    }

    @AutoRegister({ServiceType.EVENT_LISTEN})
    public static final class PaperListener extends ModuleComponent<ChatSync> {
        @Override
        public void checkCompatibility() throws APIIncompatibleException {
            Compatibility.requireClass(() -> Class.forName("io.papermc.paper.event.player.AsyncChatEvent"));
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onChat(AsyncChatEvent event) {
            this.parent.send(event.getPlayer(), event.message());
        }
    }
}
