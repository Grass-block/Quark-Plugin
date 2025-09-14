package org.atcraftmc.starlight.internal;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.atcraftmc.starlight.api.event.ChatForwardingEvent;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.service.SLService;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceInject;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@SLService(id = "chat-forwarding")
public interface ChatForwardingService extends Service {
    Listener EVENT_HANDLER = createImplementation();

    @ServiceInject
    static void start() {
        BukkitUtil.registerEventListener(EVENT_HANDLER);
    }

    @ServiceInject
    static void stop() {
        BukkitUtil.unregisterEventListener(EVENT_HANDLER);
    }

    static Listener createImplementation() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
            return new PaperEventHandler();
        } catch (Throwable ignored) {
            return new BukkitEventHandler();
        }
    }

    class PaperEventHandler implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChatting(AsyncChatEvent event) {
            if(event.isCancelled()){
                return;
            }

            event.setCancelled(true);

            for (var player : Bukkit.getOnlinePlayers()) {
                var msg = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), player);

                var e = new ChatForwardingEvent(player, event.getPlayer(), msg);
                BukkitUtil.callEvent(e);

                if (e.isCancelled()) {
                    continue;
                }

                TextSender.sendMessage(player, e.getExamined());
            }

            var e = new ChatForwardingEvent(Bukkit.getConsoleSender(), event.getPlayer(), event.message());
            BukkitUtil.callEvent(e);

            if (e.isCancelled()) {
                return;
            }

            System.out.printf("<%s> %s%n", event.getPlayer().getName(), ComponentSerializer.plain(e.getExamined()));
        }
    }

    class BukkitEventHandler implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChatting(AsyncPlayerChatEvent event) {
            if (event.isCancelled()) {
                return;
            }

            event.setCancelled(true);
            for (var player : Bukkit.getOnlinePlayers()) {
                var msg = event.getFormat().formatted(event.getPlayer(), event.getMessage());

                var e = new ChatForwardingEvent(player, event.getPlayer(), Component.text(msg));
                BukkitUtil.callEvent(e);

                if (e.isCancelled()) {
                    continue;
                }

                TextSender.sendMessage(player, e.getExamined());
            }

            var msg = event.getFormat().formatted(event.getPlayer(), event.getMessage());

            var e = new ChatForwardingEvent(Bukkit.getConsoleSender(), event.getPlayer(), Component.text(msg));
            BukkitUtil.callEvent(e);

            if (e.isCancelled()) {
                return;
            }

            System.out.printf("<%s> %s%n", event.getPlayer().getName(), ComponentSerializer.plain(e.getExamined()));
        }
    }
}
