package org.atcraftmc.quark.display;

import me.gb2022.apm.local.MappedQueryEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.apm.local.PluginMessenger;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerKickEvent;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Locale;

@AutoRegister({ServiceType.EVENT_LISTEN, ServiceType.PLUGIN_MESSAGE})
@QuarkModule(version = "1.0.0")
public final class CustomKickMessage extends PackageModule {

    @Inject
    private LanguageEntry language;

    @EventHandler(priority = EventPriority.HIGH)
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().startsWith("\u0002")) {
            event.setReason(event.getReason().replaceFirst("\u0002", ""));
            return;
        }
        String msg = this.language.buildTemplate(Language.locale(event.getPlayer()), Language.generateTemplate(this.getConfig(), "ui"));
        event.setReason(msg.replace("{reason}", event.getReason()));
    }

    @PluginMessageHandler(PluginMessenger.FETCH_KICK_MESSAGE)
    public void onMessageFetch(MappedQueryEvent event) {
        String msg = event.getProperty("message", String.class);
        if (msg.startsWith("\u0002")) {
            event.setProperty("message", msg.replaceFirst("\u0002", ""));
            return;
        }

        Locale locale = Language.locale(event.getProperty("locale", String.class));
        String ui = this.language.buildTemplate(locale, Language.generateTemplate(this.getConfig(), "ui"));
        event.setProperty("message", ui.replace("{reason}", msg));
    }
}
