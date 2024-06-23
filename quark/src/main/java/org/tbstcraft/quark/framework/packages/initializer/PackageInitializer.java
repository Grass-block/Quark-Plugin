package org.tbstcraft.quark.framework.packages.initializer;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.config.Language;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

public interface PackageInitializer {
    default void onInitialize(Plugin owner) {
    }

    default void initialize(Plugin owner) {
        this.onInitialize(owner);
    }

    default Configuration createConfig(AbstractPackage pkg) {
        return new Configuration(pkg.getOwner(), pkg.getId());
    }

    default Language createLanguage(AbstractPackage pkg) {
        return Language.create(pkg.getOwner(), pkg.getId());
    }

    String getId(AbstractPackage pkg);

    ModuleRegistry getModuleRegistry(AbstractPackage pkg);

    ServiceRegistry getServiceRegistry(AbstractPackage pkg);

    FeatureAvailability getAvailability(AbstractPackage pkg);
}
