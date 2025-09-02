package org.atcraftmc.starlight.framework.service.providing;

import org.atcraftmc.starlight.framework.packages.IPackage;
import org.atcraftmc.starlight.framework.service.Service;

import java.util.Set;

public final class DirectServiceRegistry extends ServiceRegistry {
    private final Set<Class<? extends Service>> services;

    public DirectServiceRegistry(IPackage pkg, Set<Class<? extends Service>> services) {
        super(pkg);
        this.services = services;
    }

    @Override
    public void create(Set<Class<? extends Service>> services) {
        services.addAll(this.services);
    }
}
