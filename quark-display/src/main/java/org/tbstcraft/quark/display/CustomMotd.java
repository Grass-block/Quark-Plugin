package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitUtil;

import java.io.File;

@QuarkModule
public final class CustomMotd extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onPing(ServerListPingEvent e) {
        String title = this.getLanguage().getRandomMessage("zh_cn", "motd_title");
        String subtitle = this.getLanguage().getRandomMessage("zh_cn", "motd_subtitle");

        e.setMotd(BukkitUtil.formatChatComponent(title + "{reset}{return}" + subtitle));

        try {
            e.setServerIcon(Bukkit.loadServerIcon(new File(Quark.PLUGIN.getDataFolder().getAbsolutePath() + "/motd.png")));
        } catch (Exception ignored) {
        }
    }
}