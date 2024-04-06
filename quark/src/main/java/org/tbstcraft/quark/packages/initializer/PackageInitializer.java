package org.tbstcraft.quark.packages.initializer;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.providing.ModuleRegistry;
import org.tbstcraft.quark.packages.AbstractPackage;

public interface PackageInitializer {
    default void onInitialize(Plugin owner) {
    }

    default void initialize(Plugin owner){
        this.onInitialize(owner);
    }

    default Configuration createConfig(AbstractPackage pkg) {
        return new Configuration(pkg.getOwner(),pkg.getId());
    }

    default Language createLanguage(AbstractPackage pkg) {
        return Language.create(pkg.getOwner(),pkg.getId());
    }

    String getId(AbstractPackage pkg);

    ModuleRegistry getRegistry(AbstractPackage pkg);

    FeatureAvailability getAvailability(AbstractPackage pkg);
}
