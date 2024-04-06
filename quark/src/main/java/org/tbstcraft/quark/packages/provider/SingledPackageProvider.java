package org.tbstcraft.quark.packages.provider;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.packages.initializer.PackageInitializer;

public abstract class SingledPackageProvider extends JavaPlugin implements PackageProvider {
    public abstract PackageInitializer createInitializer();
}
