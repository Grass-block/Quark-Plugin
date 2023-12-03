package org.tbstcraft.quark.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;

@QuarkModule
public final class KickOnReload extends PluginModule {
    @Override
    public void onEnable() {
        this.registerListener();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
    }

    @EventHandler
    public void onReloadCommand(ServerCommandEvent event) {
        this.handle(event.getCommand());
    }

    @EventHandler
    public void onReloadCommand(PlayerCommandPreprocessEvent event) {
        this.handle(event.getMessage());
    }

    public void handle(String command) {
        if (!command.startsWith("reload")&&!command.startsWith("/reload")) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.getConfig().getBoolean("op_ignore") && p.isOp()) {
                continue;
            }
            p.kickPlayer(this.getLanguage().getMessage(p.getLocale(),"kick_message"));
        }
    }
}
