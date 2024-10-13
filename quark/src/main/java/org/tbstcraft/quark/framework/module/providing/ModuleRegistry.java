package org.tbstcraft.quark.framework.module.providing;

import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.module.ModuleMeta;
import org.tbstcraft.quark.framework.packages.IPackage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ModuleRegistry {
    private final HashSet<AbstractModule> modules = new HashSet<>();
    private final IPackage pkg;

    protected final Collection<ModuleMeta> metas = new HashSet<>();

    protected ModuleRegistry(IPackage pkg) {
        this.pkg = pkg;
    }

    public HashSet<AbstractModule> getModules() {
        return modules;
    }

    public Collection<ModuleMeta> getMetas() {
        return metas;
    }

    public final void register(ModuleManager moduleManager) {
        for (var meta : this.metas) {
            moduleManager.registerMeta(meta);
        }


        /*
        for (AbstractModule module : this.modules) {
            try {
                if (!module.getAvailability().load()) {
                    continue;
                }
                moduleManager.register(module);
            } catch (Exception e) {
                this.getPackage().getLogger().severe("failed to register module %s: %s".formatted(
                        module.getClass().getName(), e.getMessage()));
            }
        }

         */
    }

    public final void unregister(ModuleManager moduleManager) {
        for (var meta : this.metas) {
            moduleManager.unregister(meta.fullId());
        }
    }


    protected final ClassLoader getLoader() {
        return this.getPackage().getClass().getClassLoader();
    }

    protected final IPackage getPackage() {
        return this.pkg;
    }

    public final String getId() {
        return this.pkg.getId();
    }

    public abstract void create(Set<AbstractModule> moduleList);

    public abstract void create(Collection<ModuleMeta> list);
}
