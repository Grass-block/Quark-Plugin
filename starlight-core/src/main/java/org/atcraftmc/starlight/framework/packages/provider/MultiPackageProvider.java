package org.atcraftmc.starlight.framework.packages.provider;

import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.SLPluginConcept;
import org.atcraftmc.starlight.framework.packages.PluginPackage;
import org.atcraftmc.starlight.framework.packages.SLAbstractPackage;
import org.atcraftmc.starlight.framework.packages.SLPackageInitializer;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.atcraftmc.starlight.util.ProductMetadata;
import org.atcraftmc.starlight.util.dependency.LibraryManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public abstract class MultiPackageProvider extends JavaPlugin implements PackageProvider, SLPluginConcept {
    private final ProductMetadata metadata = ProductMetadata.createFromResource(this);
    private Set<SLAbstractPackage> packages = new HashSet<>();
    private String coreInstanceId;

    @Override
    public void onEnable() {
        LibraryManager.prepareEnvironment(this);

        this.coreInstanceId = Starlight.instance().getInstanceUUID();
        if (!this.isCoreExist()) {
            return;
        }
        this.packages = createPackages();
        for (SLAbstractPackage pkg : this.packages) {
            SLPackageManager.registerPackage(pkg);
        }
    }

    @Override
    public ClassLoader classLoader() {
        return super.getClassLoader();
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    @Override
    public ProductMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public void onDisable() {
        if (!this.isCoreContextMatch()) {
            return;
        }
        for (SLAbstractPackage pkg : this.packages) {
            SLPackageManager.unregisterPackage(pkg);
        }
    }

    @Override
    public Set<SLAbstractPackage> createPackages() {
        Set<SLAbstractPackage> pkgs = new HashSet<>();
        for (SLPackageInitializer initializer : createInitializers()) {
            pkgs.add(new PluginPackage(this, initializer));
        }

        return pkgs;
    }

    @Override
    public String getCoreInstanceId() {
        return coreInstanceId;
    }

    public abstract Set<SLPackageInitializer> createInitializers();
}
