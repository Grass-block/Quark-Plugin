package org.tbstcraft.quark.display;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.event.KickMessageFetchEvent;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

@EventListener
@QuarkModule(version = "1.0.0")
public class CustomKickMessage extends PackageModule {

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().startsWith("\u0002")) {
            event.setReason(event.getReason().replaceFirst("\u0002", ""));
            return;
        }
        String msg = this.getLanguage().buildUI(this.getConfig(), "ui", event.getPlayer().getLocale());
        event.setReason(msg.replace("{reason}", event.getReason()));
    }

    @EventHandler
    public void onMessageFetch(KickMessageFetchEvent event) {
        if (event.getMessage().startsWith("\u0002")) {
            event.setMessage(event.getMessage().replaceFirst("\u0002", ""));
            return;
        }
        String msg = this.getLanguage().buildUI(this.getConfig(), "ui", event.getLocale());
        event.setMessage(msg.replace("{reason}", event.getMessage()));
    }
}
