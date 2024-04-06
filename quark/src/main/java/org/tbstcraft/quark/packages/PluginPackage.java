package org.tbstcraft.quark.packages;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;

import java.util.logging.Logger;

public class PluginPackage extends AbstractPackage {
    private final Plugin owning;

    public PluginPackage(Plugin owning, PackageInitializer initializer) {
        super(initializer);
        this.owning = owning;
    }

    @Override
    public String getLoggerName() {
        return this.owning.getDescription().getPrefix();
    }

    @Override
    public Logger getLogger() {
        return this.owning.getLogger();
    }

    @Override
    public Plugin getOwner() {
        return this.owning;
    }


}
