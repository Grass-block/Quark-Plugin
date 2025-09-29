package org.atcraftmc.starlight.framework.packages;

import org.atcraftmc.starlight.Starlight;
import org.bukkit.plugin.Plugin;

public final class InternalPackage extends PluginPackage {
    public InternalPackage(SLPackageInitializer initializer) {
        super(Starlight.instance(), initializer);
    }

    @Override
    public Plugin getOwner() {
        return Starlight.instance();
    }
}
