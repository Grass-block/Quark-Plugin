package org.tbstcraft.quark.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

@QuarkModule
public final class ChatFilter extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler(priority = EventPriority.LOWEST)
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
