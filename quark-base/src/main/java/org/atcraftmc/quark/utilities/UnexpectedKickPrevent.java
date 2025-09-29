package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import me.gb2022.modular.module.ApplicationModule;

@ApplicationModule
@AutoRegister(Registrations.SERVER_EVENT)
public class UnexpectedKickPrevent extends SLPackageModule {

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(event.getReason().contains("Expanded Storage")){
            this.handle().getLogger().info("Discarded kick request from mod EXPANDED_STORAGE.");
            event.setCancelled(true);
        }
    }

}
