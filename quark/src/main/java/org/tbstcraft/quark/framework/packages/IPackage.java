package org.tbstcraft.quark.framework.packages;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

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
