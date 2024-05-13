package org.tbstcraft.quark.framework.module.providing;

import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.util.Map;
import java.util.Set;

public final class DirectModuleRegistry extends ModuleRegistry {
    private final Map<String, Class<? extends AbstractModule>> modules;

    public DirectModuleRegistry(IPackage pkg, Map<String, Class<? extends AbstractModule>> modules) {
        super(pkg);
        this.modules = modules;
        this.create(this.getModules());
    }

    @Override
    public void create(Set<AbstractModule> moduleList) {
        for (String key : this.modules.keySet()) {
            Class<? extends AbstractModule> clazz = this.modules.get(key);

            try {
                PackageModule m = (PackageModule) clazz.getDeclaredConstructor().newInstance();
                m.init(key, this.getPackage());
                moduleList.add(m);
            } catch (Throwable e) {
                ExceptionUtil.log(e);
                this.getPackage().getLogger().warning("failed to construct module %s: %s".formatted(key, e.getMessage()));
            }
        }
    }
}
