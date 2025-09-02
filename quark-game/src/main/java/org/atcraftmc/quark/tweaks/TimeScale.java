package org.atcraftmc.quark.tweaks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;

@SLModule(version = "0.1", beta = true)
public class TimeScale extends PackageModule {
    @Override
    public void enable() {
        for (World world : Bukkit.getWorlds()) {

        }
        super.enable();
    }

    @Override
    public void disable() {
        super.disable();
    }


}
