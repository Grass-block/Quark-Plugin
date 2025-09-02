package org.atcraftmc.starlight.framework.module.providing;

import org.atcraftmc.starlight.framework.module.AbstractModule;
import org.atcraftmc.starlight.framework.module.ModuleMeta;
import org.atcraftmc.starlight.framework.packages.IPackage;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class DirectModuleRegistry extends ModuleRegistry {
    private final Map<String, Class<? extends AbstractModule>> modules;

    public DirectModuleRegistry(IPackage pkg, Map<String, Class<? extends AbstractModule>> modules) {
        super(pkg);
        this.modules = modules;
        this.create(this.metas);
    }

    @Override
    public void create(Set<AbstractModule> moduleList) {

    }

    @Override
    public void create(Collection<ModuleMeta> list) {
        for (String key : this.modules.keySet()) {
            var clazz = this.modules.get(key);

            list.add(ModuleMeta.create((Class<AbstractModule>) clazz, this.getPackage(), key));
        }
    }
}
