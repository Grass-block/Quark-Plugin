package org.atcraftmc.starlight.framework.packages;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.registry.ContentRegistry;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.language.LanguagePack;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public interface SLPackageInitializer extends ContentRegistry<SLModule, SLModuleHandle, SLService> {
    default void onInitialize(Plugin owner) {
    }

    default void initialize(Plugin owner) {
        this.onInitialize(owner);
    }

    Set<Configuration> createConfig(SLAbstractPackage pkg);

    Set<LanguagePack> createLanguagePack(SLAbstractPackage pkg);

    String getId();

    default boolean isEnableByDefault() {
        return true;
    }

    FeatureAvailability getAvailability();
}
