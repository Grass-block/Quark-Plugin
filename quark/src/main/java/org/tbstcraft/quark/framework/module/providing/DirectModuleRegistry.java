package org.tbstcraft.quark.framework.module.providing;

import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.util.Set;

public class DirectModuleRegistry extends ModuleRegistry {
    private final Set<Class<? extends AbstractModule>> modules;

    public DirectModuleRegistry(IPackage pkg, Set<Class<? extends AbstractModule>> modules) {
        super(pkg);
        this.modules = modules;
    }

    @Override
    public void create(Set<AbstractModule> moduleList) {
        for (Class<?> clazz : this.modules) {
            String id = clazz.getDeclaredAnnotation(QuarkModule.class).id();
            try {
                PackageModule m = (PackageModule) clazz.getDeclaredConstructor().newInstance();
                m.init(id, this.getPackage());
                moduleList.add(m);
            } catch (Throwable e) {
                ExceptionUtil.log(e);
                this.getPackage().getLogger().warning("failed to construct module %s: %s".formatted(id, e.getMessage()));
            }
        }
    }
}
