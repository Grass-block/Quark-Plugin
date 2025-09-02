package org.atcraftmc.starlight.framework.packages;

import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.framework.module.providing.ModuleRegistry;
import org.atcraftmc.starlight.framework.service.providing.ServiceRegistry;

import java.util.logging.Logger;

public interface IPackage {
    String getId();

    QuarkPackage getDescriptor();

    String getLoggerName();

    Logger getLogger();

    void onEnable();

    void onDisable();

    ModuleRegistry getModuleRegistry();

    ServiceRegistry getServiceRegistry();

    FeatureAvailability getAvailability();

    Plugin getOwner();
}
