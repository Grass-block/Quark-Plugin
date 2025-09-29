package org.atcraftmc.starlight;

import org.atcraftmc.starlight.framework.packages.SLAbstractPackage;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.atcraftmc.starlight.framework.packages.PluginPackage;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.provider.PackageProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class BundledPackageProvider implements PackageProvider {
    private Set<SLAbstractPackage> packages = new HashSet<>();
    private String coreInstanceId;

    public void onEnable() {
        this.coreInstanceId = Starlight.instance().getInstanceUUID();
        this.packages = createPackages();
        for (SLAbstractPackage pkg : this.packages) {
            SLPackageManager.registerPackage(pkg);
        }
    }

    public void onDisable() {
        for (var pkg : this.packages) {
            SLPackageManager.unregisterPackage(pkg);
        }
    }

    @Override
    public Set<SLAbstractPackage> createPackages() {
        var packages = new HashSet<SLAbstractPackage>();
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

    public Set<SLPackageInitializer> createInitializers() {
        try {
            var packs = new Class[]{
                    Class.forName("org.atcraftmc.quark.QuarkBase"),
                    Class.forName("org.atcraftmc.quark.QuarkGame"),
                    Class.forName("org.atcraftmc.quark.QuarkWeb")};

            var set = new HashSet<SLPackageInitializer>();

            for (var pack : packs) {
                set.addAll((Collection<? extends SLPackageInitializer>) pack.getDeclaredMethod("initializers").invoke(null));
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