package org.atcraftmc.starlight.framework.packages;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.module.ModuleManagerV2;
import me.gb2022.modular.pack.AbstractPackage;
import me.gb2022.modular.service.ServiceManager;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.language.LanguagePack;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.SLModuleManager;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.framework.SLServiceManager;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;

import java.util.Set;

public abstract class SLAbstractPackage extends AbstractPackage<SLModule, SLModuleHandle, SLService> implements SLPackage {
    private final SLPackageInitializer initializer;

    private Set<LanguagePack> languagePacks;
    private Set<Configuration> configurations;

    public SLAbstractPackage(String id, FeatureAvailability availability, SLPackageInitializer registry) {
        super(id, availability, registry);
        this.initializer = registry;
    }

    @Override
    public void onEnable() {
        for (var pack : this.languagePacks) {
            pack.load();
            Starlight.lang().register(pack);
        }
        for (var cfg : this.configurations) {
            cfg.load();
            ConfigContainer.getInstance().register(cfg);
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        for (var pack : this.languagePacks) {
            Starlight.lang().unregister(pack);
        }
        for (var cfg : this.configurations) {
            ConfigContainer.getInstance().unregister(cfg);
        }
    }


    @Override
    public ModuleManagerV2<SLModule, SLModuleHandle> getModuleManager() {
        return SLModuleManager.getInstance();
    }

    @Override
    public ServiceManager<SLService> getServiceManager() {
        return SLServiceManager.INSTANCE;
    }

    public void initializePackage() {
        var initializer = this.getInitializer();
        this.initializer.onInitialize(this.getOwner());
        this.configurations = initializer.createConfig(this);
        this.languagePacks = initializer.createLanguagePack(this);
    }

    @Override
    public final SLPackageInitializer getInitializer() {
        return this.initializer;
    }
}
