package org.tbstcraft.quark.framework.packages.initializer;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.data.config.Configuration;
import org.tbstcraft.quark.framework.data.config.Language;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.providing.DirectModuleRegistry;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.providing.DirectServiceRegistry;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PackageBuilderInitializer implements PackageInitializer {
    private final String id;
    private final FeatureAvailability availability;
    private final Set<Class<? extends Service>> services = new HashSet<>();
    private final Map<String, Class<? extends AbstractModule>> modules = new HashMap<>();
    private final boolean config;
    private final boolean language;

    public PackageBuilderInitializer(String id, FeatureAvailability availability, boolean config, boolean language) {
        this.id = id;
        this.availability = availability;
        this.config = config;
        this.language = language;
    }

    @Override
    public String getId(AbstractPackage pkg) {
        return this.id;
    }

    @Override
    public Configuration createConfig(AbstractPackage pkg) {
        if (!this.config) {
            return Quark.CONFIG;
        }
        return PackageInitializer.super.createConfig(pkg);
    }

    @Override
    public Language createLanguage(AbstractPackage pkg) {
        if (!this.language) {
            return Quark.LANGUAGE;
        }
        return PackageInitializer.super.createLanguage(pkg);
    }


    @Override
    public FeatureAvailability getAvailability(AbstractPackage pkg) {
        return this.availability;
    }

    @Override
    public ModuleRegistry getModuleRegistry(AbstractPackage pkg) {
        return new DirectModuleRegistry(pkg, this.modules);
    }

    @Override
    public ServiceRegistry getServiceRegistry(AbstractPackage pkg) {
        return new DirectServiceRegistry(pkg, this.services);
    }

    public PackageBuilderInitializer module(String id, Class<? extends AbstractModule> clazz) {
        this.modules.put(id, clazz);
        return this;
    }

    public PackageBuilderInitializer service(Class<? extends Service> clazz) {
        this.services.add(clazz);
        return this;
    }
}
