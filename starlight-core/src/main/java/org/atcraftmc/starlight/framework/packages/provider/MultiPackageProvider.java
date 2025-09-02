package org.atcraftmc.starlight.framework.packages.provider;

import org.bukkit.plugin.java.JavaPlugin;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.packages.AbstractPackage;
import org.atcraftmc.starlight.framework.packages.PackageManager;
import org.atcraftmc.starlight.framework.packages.PluginPackage;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;

import java.util.HashSet;
import java.util.Set;

public abstract class MultiPackageProvider extends JavaPlugin implements PackageProvider {
    private Set<AbstractPackage> packages = new HashSet<>();
    private String coreInstanceId;

    @Override
    public void onEnable() {
        this.coreInstanceId = Starlight.instance().getInstanceUUID();
        if (!this.isCoreExist()) {
            return;
        }
        this.packages = createPackages();
        for (AbstractPackage pkg : this.packages) {
            PackageManager.registerPackage(pkg);
        }
    }

    @Override
    public void onDisable() {
        if (!this.isCoreContextMatch()) {
            return;
        }
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

    @Override
    public String getCoreInstanceId() {
        return coreInstanceId;
    }

    public abstract Set<PackageInitializer> createInitializers();
}
