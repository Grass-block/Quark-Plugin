package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;

@SLModule
@AutoRegister(Registers.BUKKIT_EVENT)
public class UnexpectedKickPrevent extends PackageModule {

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(event.getReason().contains("Expanded Storage")){
            getL4jLogger().info("Discarded kick request from mod EXPANDED_STORAGE.");
            event.setCancelled(true);
        }
    }

}
