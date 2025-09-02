package org.atcraftmc.starlight.framework.packages.initializer;

import org.bukkit.plugin.Plugin;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.language.LanguagePack;
import org.atcraftmc.starlight.framework.module.providing.ModuleRegistry;
import org.atcraftmc.starlight.framework.packages.AbstractPackage;
import org.atcraftmc.starlight.framework.service.providing.ServiceRegistry;

import java.util.Set;

public interface PackageInitializer {
    default void onInitialize(Plugin owner) {
    }

    default void initialize(Plugin owner) {
        this.onInitialize(owner);
    }

    Set<Configuration> createConfig(AbstractPackage pkg);

    Set<LanguagePack> createLanguagePack(AbstractPackage pkg);

    String getId(AbstractPackage pkg);

    ModuleRegistry getModuleRegistry(AbstractPackage pkg);

    ServiceRegistry getServiceRegistry(AbstractPackage pkg);

    default boolean isEnableByDefault() {
        return true;
    }

    FeatureAvailability getAvailability(AbstractPackage pkg);
}
