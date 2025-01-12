package org.tbstcraft.quark;

import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.packages.PluginPackage;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.PackageProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public final class BundledPackageProvider implements PackageProvider {
    private Set<AbstractPackage> packages = new HashSet<>();
    private String coreInstanceId;

    public void onEnable() {
        this.coreInstanceId = Quark.getInstance().getInstanceUUID();
        this.packages = createPackages();
        for (AbstractPackage pkg : this.packages) {
            PackageManager.registerPackage(pkg);
        }
    }

    public void onDisable() {
        for (AbstractPackage pkg : this.packages) {
            PackageManager.unregisterPackage(pkg);
        }
    }

    @Override
    public Set<AbstractPackage> createPackages() {
        Set<AbstractPackage> pkgs = new HashSet<>();
        for (PackageInitializer initializer : createInitializers()) {
            pkgs.add(new PluginPackage(Quark.getInstance(), initializer));
        }

        return pkgs;
    }

    @Override
    public Logger getLogger() {
        return Quark.getInstance().getLogger();
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