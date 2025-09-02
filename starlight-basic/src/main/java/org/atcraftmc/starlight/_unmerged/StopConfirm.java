package org.atcraftmc.starlight._unmerged;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.util.List;

@SLModule
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class StopConfirm extends PackageModule {
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!List.of(event.getMessage().split("")).contains("/stop")) {
            return;
        }
        if (event.getMessage().contains("confirm")) {
            event.setMessage(event.getMessage().replace("confirm", ""));
            return;
        }
        event.setCancelled(true);

        MessageAccessor.send(this.getLanguage(), event.getPlayer(), "hint");
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        if (!event.getCommand().startsWith("stop")) {
            return;
        }
        if (event.getCommand().contains("confirm")) {
            event.setCommand(event.getCommand().replace("confirm", ""));
            return;
        }
        event.setCancelled(true);

        MessageAccessor.send(this.getLanguage(), event.getSender(), "hint");
    }
}
