package org.atcraftmc.starlight.framework.packages;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public class PluginPackage extends SLAbstractPackage {
    private final Plugin owning;

    public PluginPackage(Plugin owning, SLPackageInitializer initializer) {
        super(initializer.getId(), initializer.getAvailability(), initializer);
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
