package org.atcraftmc.starlight.management;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.LocaleService;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.function.Function;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(id="kick-on-reload",version = "1.0.0")
public final class KickOnReload extends SLPackageModule {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!event.getPlayer().isOp()) {
            return;
        }
        this.handle(event.getMessage().split(" ")[0].replaceFirst("/", ""));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(ServerCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.handle(event.getCommand().split(" ")[0]);
    }

    public void handle(String command) {
        if (command.equalsIgnoreCase("reload")) {
            kick((locale) -> this.handle().getLanguage().item("reload-hint").message(locale));
        }
        if (command.equalsIgnoreCase("stop")) {
            for (var p : Bukkit.getOnlinePlayers()) {
                p.kickPlayer(this.handle().getLanguage().item("stop-hint").message(LocaleService.locale(p)));
            }
        }
    }

    public void kick(Function<MinecraftLocale, String> builder) {
        for (var p : Bukkit.getOnlinePlayers()) {
            if (ConfigAccessor.getBool(this.config(), "op-ignore") && p.isOp()) {
                continue;
            }
            p.kickPlayer(builder.apply(LocaleService.locale(p)));
        }
    }
}
