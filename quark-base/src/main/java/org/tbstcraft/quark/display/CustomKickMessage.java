package org.tbstcraft.quark.display;

import me.gb2022.apm.local.MappedQueryEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.apm.local.PluginMessenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@ModuleService({ServiceType.EVENT_LISTEN,ServiceType.PLUGIN_MESSAGE})
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

    @PluginMessageHandler(PluginMessenger.FETCH_KICK_MESSAGE)
    public void onMessageFetch(MappedQueryEvent event) {
        String msg = event.getProperty("message", String.class);
        if (msg.startsWith("\u0002")) {
            event.setProperty("message", msg.replaceFirst("\u0002", ""));
            return;
        }

        String ui=this.getLanguage().buildUI(this.getConfig(), "ui", event.getProperty("locale", String.class));
        event.setProperty("message", ui.replace("{reason}", msg));
    }
}
