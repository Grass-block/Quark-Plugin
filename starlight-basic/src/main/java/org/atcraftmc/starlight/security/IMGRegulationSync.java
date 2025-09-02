package org.atcraftmc.starlight.security;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@SLModule
@AutoRegister(Registers.BUKKIT_EVENT)
public class IMGRegulationSync extends PackageModule {


    @EventHandler
    public void onPlayerJoinAttempt(AsyncPlayerPreLoginEvent event) {

    }

}
