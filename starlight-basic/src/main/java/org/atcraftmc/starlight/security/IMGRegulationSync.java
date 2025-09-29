package org.atcraftmc.starlight.security;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@ApplicationModule(id="img-regulation-sync")
@AutoRegister(Registrations.SERVER_EVENT)
public class IMGRegulationSync extends SLPackageModule {


    @EventHandler
    public void onPlayerJoinAttempt(AsyncPlayerPreLoginEvent event) {

    }

}
