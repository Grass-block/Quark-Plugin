package org.atcraftmc.starlight._unmerged;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;

import java.util.function.Function;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(version = "1.0.0")
public final class KickOnReload extends PackageModule {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!event.getPlayer().isOp()) {
            return;
        }
        this.handle(event.getMessage().split(" ")[0].replaceFirst("/", ""));
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        this.handle(event.getCommand().split(" ")[0]);
    }

    public void handle(String command) {
        if (command.equalsIgnoreCase("reload")) {
            kick((locale) -> this.getLanguage().item("reload-hint").message(locale));
        }
        if (command.equalsIgnoreCase("stop")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.kickPlayer(this.getLanguage().item("stop-hint").message(LocaleService.locale(p)));
            }
        }
    }

    public void kick(Function<MinecraftLocale, String> builder) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ConfigAccessor.getBool(this.getConfig(), "op-ignore") && p.isOp()) {
                continue;
            }
            p.kickPlayer(builder.apply(LocaleService.locale(p)));
        }
    }
}
