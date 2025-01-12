package org.atcraftmc.quark.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.framework.event.CustomEvent;
import org.tbstcraft.quark.framework.event.QuarkEvent;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;

@QuarkService(id = "chat-forwarding", requiredBy = {"quark-base:"})
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

    @QuarkEvent
    final class ChatForwardingEvent extends CustomEvent implements Cancellable {
        private final Player sender;
        private final Player viewer;
        private final Component previous;
        private Component examined;
        private boolean cancelled;

        public ChatForwardingEvent(Player sender, Player viewer, Component previous) {
            this.sender = sender;
            this.viewer = viewer;
            this.previous = previous;
            this.examined = previous;
        }

        public static HandlerList getHandlerList() {
            return getHandlerList(ChatForwardingEvent.class);
        }

        public Player getSender() {
            return sender;
        }

        public Player getViewer() {
            return viewer;
        }

        public Component getExamined() {
            return examined;
        }

        public void setExamined(Component examined) {
            this.examined = examined;
        }

        public Component getPrevious() {
            return previous;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    class PaperEventHandler implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChatting(AsyncChatEvent event) {
            event.setCancelled(true);


            for (var player : Bukkit.getOnlinePlayers()) {
                var msg = event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), player);

                var e = new ChatForwardingEvent(player, event.getPlayer(), msg);
                Bukkit.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    continue;
                }

                TextSender.sendMessage(player, e.getExamined());
            }
        }
    }

    class BukkitEventHandler implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onChatting(AsyncPlayerChatEvent event) {
            event.setCancelled(true);
            for (var player : Bukkit.getOnlinePlayers()) {
                var msg = event.getFormat().formatted(event.getPlayer(), event.getMessage());

                var e = new ChatForwardingEvent(player, event.getPlayer(), Component.text(msg));
                Bukkit.getPluginManager().callEvent(e);

                if (e.isCancelled()) {
                    continue;
                }

                TextSender.sendMessage(player, e.getExamined());
            }
        }
    }
}
