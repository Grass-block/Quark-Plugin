package org.tbstcraft.quark.packages.initializer;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.AbstractModule;
import org.tbstcraft.quark.module.providing.DirectModuleRegistry;
import org.tbstcraft.quark.module.providing.ModuleRegistry;
import org.tbstcraft.quark.packages.AbstractPackage;
import org.tbstcraft.quark.packages.QuarkPackage;

import java.util.Set;

public final class ClassInitializer implements PackageInitializer {
    Class<? extends AbstractModule>[] classes;

    @SafeVarargs
    public ClassInitializer(Class<? extends AbstractModule>... classes) {
        this.classes = classes;
    }

    private QuarkPackage getAnnotation(AbstractPackage pkg) {
        return pkg.getClass().getAnnotation(QuarkPackage.class);
    }

    @Override
    public String getId(AbstractPackage pkg) {
        return getAnnotation(pkg).value();
    }

    @Override
    public Configuration createConfig(AbstractPackage pkg) {
        if (!this.getAnnotation(pkg).config()) {
            return null;
        }
        return PackageInitializer.super.createConfig(pkg);
    }

    @Override
    public Language createLanguage(AbstractPackage pkg) {
        if (!this.getAnnotation(pkg).lang()) {
            return null;
        }
        return PackageInitializer.super.createLanguage(pkg);
    }

    @Override
    public ModuleRegistry getRegistry(AbstractPackage pkg) {
        return new DirectModuleRegistry(pkg, Set.of(this.classes));
    }


    @Override
    public FeatureAvailability getAvailability(AbstractPackage pkg) {
        return this.getAnnotation(pkg).available();
    }
}
