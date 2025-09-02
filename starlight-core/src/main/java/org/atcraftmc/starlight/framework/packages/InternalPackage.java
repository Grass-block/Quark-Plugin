package org.atcraftmc.starlight.framework.packages;

import org.atcraftmc.starlight.Starlight;
import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;

public final class InternalPackage extends PluginPackage {
    public InternalPackage(PackageInitializer initializer) {
        super(Starlight.instance(), initializer);
    }

    @Override
    public Plugin getOwner() {
        return Starlight.instance();
    }
}
