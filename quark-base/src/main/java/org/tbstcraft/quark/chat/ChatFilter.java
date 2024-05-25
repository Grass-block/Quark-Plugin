package org.tbstcraft.quark.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-filter",version = "1.0.0")
public final class ChatFilter extends PackageModule {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatting(AsyncPlayerChatEvent event) {
        String msg = this.filter(event.getMessage());
        event.setMessage(msg);
    }

    @EventHandler
    public void detectCommand(PlayerCommandPreprocessEvent event) {
        if (!(event.getMessage().contains("say") || event.getMessage().contains("tell"))) {
            return;
        }
        event.setMessage(this.filter(event.getMessage()));
    }

    public String filter(String msg) {
        for (String key : this.getConfig().getStringList("keywords")) {
            msg = msg.replace(key, "*".repeat(key.length()));
        }
        return msg;
    }
}
