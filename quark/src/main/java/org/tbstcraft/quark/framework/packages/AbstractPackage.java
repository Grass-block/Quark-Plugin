package org.tbstcraft.quark.framework.packages;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.config.Language;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.service.ServiceManager;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

public abstract class AbstractPackage implements IPackage {
    private final PackageInitializer initializer;
    private String id;
    private Language languageFile;
    private Configuration configFile;
    private FeatureAvailability availability;
    private ModuleRegistry moduleRegistry;
    private ServiceRegistry serviceRegistry;

    protected AbstractPackage(PackageInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void onEnable() {
        if (this.getServiceRegistry() != null) {
            getServiceRegistry().register(ServiceManager.INSTANCE);
        }
        getModuleRegistry().register(ModuleManager.getInstance());
    }

    @Override
    public void onDisable() {
        getModuleRegistry().unregister(ModuleManager.getInstance());
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
    public final ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
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
        this.moduleRegistry = initializer.getModuleRegistry(this);
        this.serviceRegistry = initializer.getServiceRegistry(this);
    }

    public final PackageInitializer getInitializer() {
        return this.initializer;
    }
}
