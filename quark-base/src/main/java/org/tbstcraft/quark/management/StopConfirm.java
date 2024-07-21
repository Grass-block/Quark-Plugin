package org.tbstcraft.quark.management;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class StopConfirm extends PackageModule {
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/stop")) {
            return;
        }
        if (event.getMessage().contains("confirm")) {
            event.setMessage(event.getMessage().replace("confirm",""));
            return;
        }
        event.setCancelled(true);

        getLanguage().sendMessage(event.getPlayer(), "hint");
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        if (!event.getCommand().startsWith("stop")) {
            return;
        }
        if (event.getCommand().contains("confirm")) {
            event.setCommand(event.getCommand().replace("confirm",""));
            return;
        }
        event.setCancelled(true);

        getLanguage().sendMessage(event.getSender(), "hint");
    }
}
