package org.tbstcraft.quark;

import org.bukkit.Bukkit;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.framework.packages.PluginPackage;
import org.tbstcraft.quark.framework.packages.initializer.JsonPackageInitializer;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.framework.packages.provider.PackageProvider;

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
        return Set.of(
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_security.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_display.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_chat.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_utilities.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_automatic.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-management.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-tweaks.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-storage.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-contents.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-warps.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_proxysupport.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark_clientsupport.json"),
                new JsonPackageInitializer(FeatureAvailability.PREMIUM, "/packages/quark-web.json")
                     );
    }


    public boolean isPresent() {
        return getClass().getResourceAsStream("/bundler.flag") != null;
    }
}