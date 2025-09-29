package org.atcraftmc.starlight.framework.packages;

import me.gb2022.commons.container.Pair;
import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.registry.BuilderContentRegistry;
import org.atcraftmc.qlib.config.Configuration;
import org.atcraftmc.qlib.language.LanguagePack;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.SLService;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class SLPackageBuilder extends BuilderContentRegistry<SLModule, SLModuleHandle, SLService> implements SLPackageInitializer {
    private final Set<Pair<String, String>> packs = new HashSet<>();
    private final Set<String> configs = new HashSet<>();

    private final String id;
    private final FeatureAvailability availability;

    public SLPackageBuilder(String id, FeatureAvailability availability) {
        this.id = id;
        this.availability = availability;
    }

    public static SLPackageBuilder of(String id, FeatureAvailability availability, Consumer<SLPackageBuilder> handler) {
        var i = new SLPackageBuilder(id, availability);
        handler.accept(i);
        return i;
    }

    @Override
    public SLPackageBuilder module(String id, Class<? extends SLModule> clazz) {
        super.module(id, clazz);
        return this;
    }

    @Override
    public SLPackageBuilder service(Class<? extends SLService> clazz) {
        super.service(clazz);
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Set<Configuration> createConfig(SLAbstractPackage pkg) {
        Set<Configuration> configs = new HashSet<>();
        for (var id : this.configs) {
            configs.add(Configurations.values(Starlight.SubPackPluginConceptWrapper.of(pkg.getOwner()), id));
        }

        return configs;
    }

    @Override
    public Set<LanguagePack> createLanguagePack(SLAbstractPackage pkg) {
        Set<LanguagePack> packs = new HashSet<>();
        for (Pair<String, String> pack : this.packs) {
            packs.add(new LanguagePack(pack.getLeft(), pack.getRight(), Starlight.SubPackPluginConceptWrapper.of(pkg.getOwner())));
        }

        return packs;
    }

    @Override
    public FeatureAvailability getAvailability() {
        return this.availability;
    }

    public SLPackageBuilder language(String name, String lang) {
        this.packs.add(new Pair<>(name, lang));
        return this;
    }

    public SLPackageBuilder config(String config) {
        this.configs.add(config);
        return this;
    }

    @Override
    public SLModuleHandle wrapMeta(IPackage<SLModule, SLModuleHandle, SLService> owner, Class<? extends SLModule> mc) {
        return new SLModuleHandle((SLPackage) owner, mc);
    }
}
