package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.md_5.bungee.api.chat.BaseComponent;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.atcraftmc.starlight.core.PlayerView;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.core.TaskService;

@SLModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ActionBarHUD extends PackageModule {
    @Inject
    private LanguageEntry language;

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(() -> Player.class.getDeclaredMethod("sendActionBar", BaseComponent[].class));
        Compatibility.blackListPlatform(APIProfile.ARCLIGHT, APIProfile.BANNER, APIProfile.YOUER);
    }

    private String render(Player player) {
        var loc = player.getLocation();
        var block = loc.getBlock();

        var locale = LocaleService.locale(player);
        var biome_n = block.getBiome().getKey().getNamespace();
        var biome_k = block.getBiome().getKey().getKey();

        var p = MessageAccessor.getMessage(this.language, locale, "position", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        var b = MessageAccessor.getMessage(this.language, locale, "biome", biome_n, biome_k);
        var t = MessageAccessor.getMessage(this.language, locale, "time");
        var f = MessageAccessor.getMessage(
                this.language,
                locale,
                "face",
                SharedObjects.NUMBER_FORMAT.format(loc.getYaw()),
                SharedObjects.NUMBER_FORMAT.format(loc.getPitch())
        );

        var template = getConfig().value("template")
                .string()
                .replace("{position}", p)
                .replace("{biome}", b)
                .replace("{time}", t)
                .replace("{face}", f);

        return PlaceHolderService.formatPlayer(player, template);
    }

    private void startRender(Player player) {
        PlayerView.getInstance(player).getActionbar().addChannel("quark:actionbar-hud", -10, 3, TaskService::entity, (p, c) -> {
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        startRender(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopRender(event.getPlayer());
    }
}
