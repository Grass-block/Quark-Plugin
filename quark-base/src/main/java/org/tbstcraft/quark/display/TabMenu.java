package org.tbstcraft.quark.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.task.TaskService;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.api.PlayerUtil;

@EventListener
@QuarkModule(version = "2.0.3")
public final class TabMenu extends PackageModule {
    public static final String UPDATE_TASK_TID = "quark_display:tab_menu:update";

    @Override
    public void enable() {
        TaskService.asyncTimerTask(UPDATE_TASK_TID, 0, 20, this::update);
        BukkitUtil.registerEventListener(this);
        this.update();
    }

    @Override
    public void disable() {
        TaskService.cancelTask(UPDATE_TASK_TID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerUtil.setPlayerTab(p, "", "");
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
        String locale = Language.getLocale(player);

        String header = this.getLanguage().buildUI(this.getConfig(), "header-ui", locale);
        String footer = this.getLanguage().buildUI(this.getConfig(), "footer-ui", locale);
        PlayerUtil.setPlayerTab(player, header, footer);
    }
}
