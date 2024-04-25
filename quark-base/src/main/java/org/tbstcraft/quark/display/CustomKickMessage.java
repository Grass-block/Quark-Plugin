package org.tbstcraft.quark.display;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.framework.event.messenging.MappedQueryEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.EventListener;

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
    public void onMessageFetch(MappedQueryEvent event) {
        String msg = event.getProperty("message", String.class);
        if (msg.startsWith("\u0002")) {
            event.setProperty("message", msg.replaceFirst("\u0002", ""));
            return;
        }

        msg = this.getLanguage().buildUI(this.getConfig(), "ui", event.getProperty("locale", String.class));
        event.setProperty("message", msg.replace("{reason}", msg));
    }
}
