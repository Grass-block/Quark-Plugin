package org.tbstcraft.quark.framework.packages;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;

public final class InternalPackage extends PluginPackage {
    public InternalPackage(PackageInitializer initializer) {
        super(Quark.PLUGIN, initializer);
    }

    @Override
    public Plugin getOwner() {
        return Quark.PLUGIN;
    }
}
