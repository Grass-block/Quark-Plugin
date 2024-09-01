package org.tbstcraft.quark.framework.packages.provider;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.packages.initializer.PackageInitializer;

public abstract class SingledPackageProvider extends JavaPlugin implements PackageProvider {
    private String coreInstanceId;

    public abstract PackageInitializer createInitializer();

    @Override
    public void onEnable() {
        this.coreInstanceId = Quark.getInstance().getInstanceUUID();
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
