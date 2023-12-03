package org.tbstcraft.quark.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.config.LanguageFile;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitUtil;


@QuarkModule
public class ChatComponent extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onChatting(AsyncPlayerChatEvent event) {
        String msg = BukkitUtil.formatChatComponent(LanguageFile.formatGlobal(event.getMessage()));
        event.setMessage(msg);
    }
}
