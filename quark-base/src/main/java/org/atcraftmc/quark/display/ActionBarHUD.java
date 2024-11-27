package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.PlayerView;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.foundation.text.TextSender;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.internal.placeholder.PlaceHolderService;
import org.tbstcraft.quark.internal.task.TaskService;

@QuarkModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ActionBarHUD extends PackageModule {
    @Inject
    private LanguageEntry language;

    private String render(Player player) {
        var loc = player.getLocation();
        var block = loc.getBlock();

        var locale = LocaleService.locale(player);

        var p = this.language.getMessage(locale, "position", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        var b = this.language.getMessage(locale, "biome", (Object[]) block.getBiome().getKey().asString().split(":"));
        var t = this.language.getMessage(locale, "time");
        var f = this.language.getMessage(
                locale,
                "face",
                SharedObjects.NUMBER_FORMAT.format(loc.getYaw()),
                SharedObjects.NUMBER_FORMAT.format(loc.getPitch())
        );

        var template = getConfig().getString("template")
                .replace("{position}", p)
                .replace("{biome}", b)
                .replace("{time}", t)
                .replace("{face}", f);

        return PlaceHolderService.formatPlayer(player, template);
    }


    private void startRender(Player player) {
        PlayerView.getInstance(player).getActionbar().addChannel("quark:actionbar-hud", -10, 3, TaskService.async(), (p, c) -> {
            var comp = TextBuilder.buildComponent(render(p));
            TextSender.sendActionbarTitle(p, comp);
        });
    }

    private void stopRender(Player player) {
        PlayerView.getInstance(player).getActionbar().removeChannel("quark:actionbar-hud");
    }


    @Override
    public void enable() {
        for (var player : Bukkit.getOnlinePlayers()) {
            startRender(player);
        }
    }

    @Override
    public void disable() {
        for (var player : Bukkit.getOnlinePlayers()) {
            stopRender(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        startRender(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopRender(event.getPlayer());
    }
}
