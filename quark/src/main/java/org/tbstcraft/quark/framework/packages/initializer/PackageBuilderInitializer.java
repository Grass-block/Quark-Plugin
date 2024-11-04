package org.tbstcraft.quark.framework.packages.initializer;

import me.gb2022.commons.container.Pair;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.language.LanguagePack;
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
import java.util.function.Consumer;

public final class PackageBuilderInitializer implements PackageInitializer {
    private final Set<Class<? extends Service>> services = new HashSet<>();
    private final Set<Pair<String, String>> packs = new HashSet<>();
    private final Map<String, Class<? extends AbstractModule>> modules = new HashMap<>();
    private final Set<String> configs = new HashSet<>();

    private final String id;
    private final FeatureAvailability availability;

    public PackageBuilderInitializer(String id, FeatureAvailability availability) {
        this.id = id;
        this.availability = availability;
    }

    public static PackageBuilderInitializer of(String id, FeatureAvailability availability, Consumer<PackageBuilderInitializer> handler) {
        var i = new PackageBuilderInitializer(id, availability);
        handler.accept(i);
        return i;
    }

    @Override
    public String getId(AbstractPackage pkg) {
        return this.id;
    }

    @Override
    public Set<Configuration> createConfig(AbstractPackage pkg) {
        Set<Configuration> configs = new HashSet<>();
        for (var id : this.configs) {
            configs.add(new Configuration(pkg.getOwner(), id));
        }

        return configs;
    }

    @Override
    public Set<LanguagePack> createLanguagePack(AbstractPackage pkg) {
        Set<LanguagePack> packs = new HashSet<>();
        for (Pair<String, String> pack : this.packs) {
            packs.add(new LanguagePack(pack.getLeft(), pack.getRight(), pkg.getOwner()));
        }

        return packs;
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

    public PackageBuilderInitializer language(String name, String lang) {
        this.packs.add(new Pair<>(name, lang));
        return this;
    }

    public PackageBuilderInitializer config(String config) {
        this.configs.add(config);
        return this;
    }
}
