package org.atcraftmc.starlight.framework.packages;

import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.language.LanguagePack;
import org.atcraftmc.starlight.framework.module.ModuleManager;
import org.atcraftmc.starlight.framework.module.providing.ModuleRegistry;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.service.ServiceManager;
import org.atcraftmc.starlight.framework.service.providing.ServiceRegistry;

import java.util.Set;

public abstract class AbstractPackage implements IPackage {
    private final PackageInitializer initializer;
    private String id;
    private FeatureAvailability availability;
    private ModuleRegistry moduleRegistry;
    private ServiceRegistry serviceRegistry;

    private Set<LanguagePack> languagePacks;
    private Set<Configuration> configurations;

    protected AbstractPackage(PackageInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public void onEnable() {
        for (LanguagePack pack : this.languagePacks) {
            pack.load();
            Starlight.lang().register(pack);
        }
        for (Configuration cfg : this.configurations) {
            cfg.load();
            ConfigContainer.getInstance().register(cfg);
        }

        if (this.getServiceRegistry() != null) {
            getServiceRegistry().register(ServiceManager.INSTANCE);
        }
        getModuleRegistry().register(ModuleManager.getInstance());
    }

    @Override
    public void onDisable() {
        getModuleRegistry().unregister(ModuleManager.getInstance());
        if (this.getServiceRegistry() != null) {
            getServiceRegistry().unregister(ServiceManager.INSTANCE);
        }

        for (LanguagePack pack : this.languagePacks) {
            Starlight.lang().unregister(pack);
        }
        for (Configuration cfg : this.configurations) {
            ConfigContainer.getInstance().unregister(cfg);
        }
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
        this.configurations = initializer.createConfig(this);
        this.moduleRegistry = initializer.getModuleRegistry(this);
        this.serviceRegistry = initializer.getServiceRegistry(this);
        this.languagePacks = initializer.createLanguagePack(this);
    }

    public final PackageInitializer getInitializer() {
        return this.initializer;
    }
}
