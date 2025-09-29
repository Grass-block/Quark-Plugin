package org.atcraftmc.starlight.framework.module;

import org.atcraftmc.starlight.framework.packages.PluginPackage;
import org.atcraftmc.starlight.framework.packages.SLPackage;
import org.atcraftmc.starlight.framework.packages.SLPackageBuilder;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public abstract class SLStandaloneModule extends JavaPlugin implements SLModule {
    private SLPackage handle;
    private SLModuleHandle mh;

    @Override
    public final void onEnable() {
        var ann = getClass().getAnnotation(StandalonePackage.class);

        var initializer = SLPackageBuilder.of(ann.value(), ann.avail(), (i) -> {
            i.module(this.getClass());
            registerContents(i);
        });

        this.handle = new PluginPackage(this, initializer);

        SLPackageManager.registerPackage(this.handle);
    }

    @Override
    public final void onDisable() {
        SLPackageManager.unregisterPackage(this.handle);
    }

    @Override
    public void init(String id, SLPackage parent, SLModuleHandle handle) {
        this.mh = handle;
    }

    @Override
    public Plugin ownerPlugin() {
        return this;
    }

    @Override
    public SLPackage parent() {
        return this.handle;
    }

    @Override
    public SLModuleHandle handle() {
        return this.mh;
    }

    public abstract void registerContents(SLPackageBuilder builder);
}
