package org.tbstcraft.quark.framework.module.providing;

import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.ModuleMeta;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.packages.IPackage;

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
