package org.tbstcraft.quark.framework.packages.initializer;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.language.ILanguageAccess;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguagePack;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

import java.util.Set;

public interface PackageInitializer {
    default void onInitialize(Plugin owner) {
    }

    default void initialize(Plugin owner) {
        this.onInitialize(owner);
    }

    default Configuration createConfig(AbstractPackage pkg) {
        return new Configuration(pkg.getOwner(), pkg.getId());
    }

    Set<LanguagePack> createLanguagePack(AbstractPackage pkg);

    default ILanguageAccess createLanguage(AbstractPackage pkg) {
        return LanguageContainer.getInstance().access(pkg.getId());
    }

    String getId(AbstractPackage pkg);

    ModuleRegistry getModuleRegistry(AbstractPackage pkg);

    ServiceRegistry getServiceRegistry(AbstractPackage pkg);

    default boolean isEnableByDefault() {
        return true;
    }

    FeatureAvailability getAvailability(AbstractPackage pkg);
}
