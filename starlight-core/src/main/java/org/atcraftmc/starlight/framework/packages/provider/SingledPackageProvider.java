package org.atcraftmc.starlight.framework.packages.provider;

import org.bukkit.plugin.java.JavaPlugin;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.packages.initializer.PackageInitializer;

public abstract class SingledPackageProvider extends JavaPlugin implements PackageProvider {
    private String coreInstanceId;

    public abstract PackageInitializer createInitializer();

    @Override
    public void onEnable() {
        this.coreInstanceId = Starlight.instance().getInstanceUUID();
        if (!this.isCoreExist()) {
            return;
        }
    }

    @Override
    public void onDisable() {
        if (!this.isCoreContextMatch()) {
            return;
        }
    }

    @Override
    public String getCoreInstanceId() {
        return coreInstanceId;
    }
}
