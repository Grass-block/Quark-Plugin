package org.atcraftmc.starlight;

import org.atcraftmc.starlight.framework.packages.AbstractPackage;
import org.atcraftmc.starlight.framework.packages.PackageManager;
import org.atcraftmc.starlight.framework.packages.PluginPackage;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.PackageProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class BundledPackageProvider implements PackageProvider {
    private Set<AbstractPackage> packages = new HashSet<>();
    private String coreInstanceId;

    public void onEnable() {
        this.coreInstanceId = Starlight.instance().getInstanceUUID();
        this.packages = createPackages();
        for (AbstractPackage pkg : this.packages) {
            PackageManager.registerPackage(pkg);
        }
    }

    public void onDisable() {
        for (var pkg : this.packages) {
            PackageManager.unregisterPackage(pkg);
        }
    }

    @Override
    public Set<AbstractPackage> createPackages() {
        var packages = new HashSet<AbstractPackage>();
        for (var initializer : createInitializers()) {
            packages.add(new PluginPackage(Starlight.instance(), initializer));
        }

        return packages;
    }

    @Override
    public Logger getLogger() {
        return Starlight.instance().getLogger();
    }

    @Override
    public String getCoreInstanceId() {
        return coreInstanceId;
    }

    public Set<PackageInitializer> createInitializers() {
        try {
            var packs = new Class[]{
                    Class.forName("org.atcraftmc.quark.QuarkBase"),
                    Class.forName("org.atcraftmc.quark.QuarkGame"),
                    Class.forName("org.atcraftmc.quark.QuarkWeb")};

            var set = new HashSet<PackageInitializer>();

            for (var pack : packs) {
                set.addAll((Collection<? extends PackageInitializer>) pack.getDeclaredMethod("initializers").invoke(null));
            }

            return set;
        } catch (Exception e) {
            e.printStackTrace();
            return Set.of();
        }
    }

    public boolean isPresent() {
        return getClass().getResourceAsStream("/bundler.flag") != null;
    }
}