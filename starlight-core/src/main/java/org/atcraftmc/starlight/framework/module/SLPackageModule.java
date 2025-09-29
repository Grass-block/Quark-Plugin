package org.atcraftmc.starlight.framework.module;

import me.gb2022.modular.module.AbstractModule;
import org.atcraftmc.starlight.framework.packages.SLPackage;
import org.bukkit.plugin.Plugin;

public abstract class SLPackageModule extends AbstractModule<SLModuleHandle, SLPackage> implements SLModule {
    @Override
    public final Plugin ownerPlugin() {
        return this.parent().getOwner();
    }
}
