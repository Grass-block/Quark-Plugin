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

    @Override
    public String getId() {
        return this.module.getId();
    }

    @Override
    public QuarkPackage getDescriptor() {
        return this.module.getClass().getAnnotation(QuarkPackage.class);
    }

}
