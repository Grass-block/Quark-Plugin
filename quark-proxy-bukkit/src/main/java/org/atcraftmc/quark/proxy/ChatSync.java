package org.atcraftmc.quark.proxy;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import me.gb2022.modular.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.subcomponent.ComponentProvider;
import org.atcraftmc.starlight.framework.module.SLModuleComponent;

import java.nio.charset.StandardCharsets;

@ApplicationModule(version = "1.0.0")
@AutoRegister({Registrations.SERVER_EVENT})
@ComponentProvider({ChatSync.BukkitListener.class, ChatSync.PaperListener.class})
public final class ChatSync extends SLPackageModule {
    @Override
    public void enable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(Starlight.instance(), "quark_plugin:msg");
    }

    public void send(Player player, Component message) {
        var payload = GsonComponentSerializer.gson().serialize(message);
        player.sendPluginMessage(Starlight.instance(), "quark_plugin:msg", payload.getBytes(StandardCharsets.UTF_8));
    }

    @AutoRegister({Registrations.SERVER_EVENT})
    public static final class BukkitListener extends SLModuleComponent<ChatSync> {
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

    @AutoRegister({Registrations.SERVER_EVENT})
    public static final class PaperListener extends SLModuleComponent<ChatSync> {
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
