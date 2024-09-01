package org.tbstcraft.quark.framework.module.standalone;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.packages.QuarkPackage;

public abstract class StandaloneModuleProvider extends JavaPlugin implements IPackage {
    private AbstractModule module;

    @Override
    public void onEnable() {
        this.module = get();
        this.module.enableModule();
    }

    @Override
    public void onDisable() {
        this.module.disableModule();
    }

    public abstract StandaloneModule get();


    // TODO: 2024/3/14 impl
    @Override
    public String getId() {
        return null;
    }

    @Override
    public QuarkPackage getDescriptor() {
        return null;
    }

}
