package org.atcraftmc.starlight.framework.packages;

import me.gb2022.modular.pack.IPackage;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.plugin.Plugin;

public interface SLPackage extends IPackage<SLModule, SLModuleHandle, SLService> {
    Plugin getOwner();

    SLPackageInitializer getInitializer();
}
