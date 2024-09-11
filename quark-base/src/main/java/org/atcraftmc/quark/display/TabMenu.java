package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "2.0.3")
public final class TabMenu extends PackageModule {
    public static final String UPDATE_TASK_TID = "quark_display:tab_menu:update";

    @Override
    public void enable() {
        TaskService.async().timer(UPDATE_TASK_TID, 0, 20, this::update);
        BukkitUtil.registerEventListener(this);
        this.update();
    }

    @Override
    public void disable() {
        TaskService.async().cancel(UPDATE_TASK_TID);
        for (Player p : Bukkit.getOnlinePlayers()) {
            Players.setPlayerTab(p, "", "");
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
        var locale = Language.locale(player);

        var header = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "header-ui"));
        var footer = this.getLanguage().buildTemplate(locale, Language.generateTemplate(this.getConfig(), "footer-ui"));

        header = PlaceHolderService.formatPlayer(player, header);
        footer = PlaceHolderService.formatPlayer(player, footer);

        Players.setPlayerTab(player, header, footer);
    }
}
