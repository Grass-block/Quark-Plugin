package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "0.2")
public final class CustomScoreboard extends PackageModule {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireClass(() -> Class.forName("org.bukkit.scoreboard.Scoreboard"));
    }

    @Override
    public void enable() {
        TaskService.global().timer("starlight:scoreboard/update", 1, 20, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                renderScoreboard(p);
            }
        });
    }

    @Override
    public void disable() {
        TaskService.global().cancel("starlight:scoreboard/update");
        for (Player p : Bukkit.getOnlinePlayers()) {
            VisualScoreboardService.instance().visualScoreboard(p).stopSidebarRendering();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        renderScoreboard(event.getPlayer());
    }

    private void renderScoreboard(Player player) {
        var board = VisualScoreboardService.instance().visualScoreboard(player);
        var locale = LocaleService.locale(player);
        var title = this.getLanguage().item("title").component(locale);
        var template = Language.generateTemplate(this.getConfig(), "ui");
        var uiRaw = MessageAccessor.buildTemplate(this.getLanguage(), locale, template).replace("{player}", player.getName());
        var uiBlock = TextBuilder.buildStringBlocks(PlaceHolderService.formatPlayer(player, uiRaw));

        board.renderSidebar(title.asComponent(), uiBlock);
    }
}
