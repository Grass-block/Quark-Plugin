package org.tbstcraft.quark.framework.module.providing;

import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.framework.module.ModuleManager;

import java.util.HashSet;
import java.util.Set;

public abstract class ModuleRegistry {
    private final HashSet<AbstractModule> modules = new HashSet<>();
    private final IPackage pkg;

    protected ModuleRegistry(IPackage pkg) {
        this.pkg = pkg;
    }

    public HashSet<AbstractModule> getModules() {
        return modules;
    }

    public final void register(ModuleManager moduleManager) {
        for (AbstractModule module : this.modules) {
            try {
                if (!module.getAvailability().load()) {
                    continue;
                }
                moduleManager.register(module, this.pkg.getLogger());
            } catch (Exception e) {
                this.getPackage().getLogger().severe("failed to register module %s: %s".formatted(
                        module.getClass().getName(), e.getMessage()));
            }
        }
    }

    public final void unregister(ModuleManager moduleManager) {
        for (AbstractModule module : this.modules) {
            try {
                if (!module.getAvailability().load()) {
                    continue;
                }
                moduleManager.unregister(module.getFullId(), this.pkg.getLogger());
            } catch (Exception e) {
                this.getPackage().getLogger().severe("failed to register module %s: %s".formatted(
                        module.getClass().getName(), e.getMessage()));
            }
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
}
