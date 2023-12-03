package org.tbstcraft.quark.display;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.ModuleDataService;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.nbt.NBTTagCompound;

@QuarkModule
public final class ChatFormat extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String template = this.getConfig().getString("template");
        if (template == null) {
            return;
        }
        String finalMsg = template.replace("{player}", "%1$s")
                .replace("{message}", "%2$s");
        event.setFormat(BukkitUtil.formatChatComponent(finalMsg));
    }
}
