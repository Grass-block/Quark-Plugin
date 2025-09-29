package org.atcraftmc.quark.tweaks;

import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.Bukkit;
import org.bukkit.World;
import me.gb2022.modular.module.ApplicationModule;

@ApplicationModule(version = "0.1", beta = true)
public class TimeScale extends SLPackageModule {
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
