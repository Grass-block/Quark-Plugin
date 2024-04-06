package org.tbstcraft.quark.packages;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.providing.ModuleRegistry;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.service.framework.ModuleManager;

public abstract class AbstractPackage implements IPackage {
    private final PackageInitializer initializer;
    private String id;
    private Language languageFile;
    private Configuration configFile;
    private FeatureAvailability availability;
    private ModuleRegistry registry;

    protected AbstractPackage(PackageInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void onEnable() {
        getRegistry().register(ModuleManager.getInstance());
    }

    @Override
    public void onDisable() {
        getRegistry().unregister(ModuleManager.getInstance());
    }

    @Override
    public final QuarkPackage getDescriptor() {
        return this.getClass().getAnnotation(QuarkPackage.class);
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Language getLanguageFile() {
        return languageFile;
    }

    @Override
    public final Configuration getConfigFile() {
        return configFile;
    }

    @Override
    public final ModuleRegistry getRegistry() {
        return registry;
    }

    @Override
    public final FeatureAvailability getAvailability() {
        return availability;
    }

    public void initializePackage() {
        PackageInitializer initializer = this.getInitializer();
        this.initializer.onInitialize(this.getOwner());
        this.availability = initializer.getAvailability(this);
        this.id = initializer.getId(this);
        this.configFile = initializer.createConfig(this);
        this.languageFile = initializer.createLanguage(this);
        this.registry = initializer.getRegistry(this);
    }

    public final PackageInitializer getInitializer() {
        return this.initializer;
    }
}
