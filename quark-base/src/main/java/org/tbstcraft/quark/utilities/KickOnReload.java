package org.tbstcraft.quark.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.data.language.Language;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.util.Locale;
import java.util.function.Function;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0.0")
public final class KickOnReload extends PackageModule {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if(!event.getPlayer().isOp()){
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
            kick((locale) -> this.getLanguage().getMessage(locale, "reload-hint"));
        }
        if (command.equalsIgnoreCase("stop")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.kickPlayer(this.getLanguage().getMessage(Language.locale(p), "stop-hint"));
            }
        }
    }

    public void kick(Function<Locale, String> builder) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.getConfig().getBoolean("op-ignore") && p.isOp()) {
                continue;
            }
            p.kickPlayer(builder.apply(Language.locale(p)));
        }
    }
}
