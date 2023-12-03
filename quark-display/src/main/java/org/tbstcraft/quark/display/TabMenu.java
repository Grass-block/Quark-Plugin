package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.TaskManager;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitUtil;

@QuarkModule
public final class TabMenu extends PluginModule {
    public static final String UPDATE_TASK_TID = "quark_display:tab_menu:update";

    @Override
    public void onEnable() {
        TaskManager.runTimer(UPDATE_TASK_TID, 0, 20, this::update);
        this.registerListener();
        this.update();
    }

    @Override
    public void onDisable() {
        this.unregisterListener();
        TaskManager.cancelTask(UPDATE_TASK_TID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setPlayerListHeaderFooter(null, null);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.setPlayerList(event.getPlayer());
    }

    private void update() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            setPlayerList(p);
        }
    }

    public void setPlayerList(Player player) {
        String locale = player.getLocale();

        player.setPlayerListHeaderFooter(
                BukkitUtil.formatPlayerHolder(player, this.getLanguage().getMessage(locale, "header")),
                BukkitUtil.formatPlayerHolder(player, this.getLanguage().getMessage(locale, "footer"))
        );
    }
}
