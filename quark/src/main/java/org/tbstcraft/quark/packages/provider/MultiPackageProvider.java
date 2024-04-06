package org.tbstcraft.quark.packages.provider;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.packages.AbstractPackage;
import org.tbstcraft.quark.packages.PluginPackage;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;
import org.tbstcraft.quark.service.framework.PackageManager;

import java.util.HashSet;
import java.util.Set;

public abstract class MultiPackageProvider extends JavaPlugin implements PackageProvider {
    private Set<AbstractPackage> packages = new HashSet<>();

    @Override
    public void onEnable() {
        this.packages = createPackages();
        for (AbstractPackage pkg : this.packages) {
            PackageManager.registerPackage(pkg);
        }
    }

    @Override
    public void onDisable() {
        for (AbstractPackage pkg : this.packages) {
            PackageManager.unregisterPackage(pkg);
        }
    }

    @Override
    public Set<AbstractPackage> createPackages() {
        Set<AbstractPackage> pkgs = new HashSet<>();
        for (PackageInitializer initializer : createInitializers()) {
            pkgs.add(new PluginPackage(this, initializer));
        }

        return pkgs;
    }

    public abstract Set<PackageInitializer> createInitializers();
}
