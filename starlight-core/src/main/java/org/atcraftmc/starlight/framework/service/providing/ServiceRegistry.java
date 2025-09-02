package org.atcraftmc.starlight.framework.service.providing;

import org.atcraftmc.starlight.framework.packages.IPackage;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.framework.service.ServiceManager;

import java.util.HashSet;
import java.util.Set;

public abstract class ServiceRegistry {
    private final HashSet<Class<? extends Service>> services = new HashSet<>();
    private final IPackage pkg;

    protected ServiceRegistry(IPackage pkg) {
        this.pkg = pkg;
    }

    public HashSet<Class<? extends Service>> getServices() {
        return services;
    }

    public final void register(ServiceManager serviceManager) {
        this.create(this.services);
        for (Class<? extends Service> service : this.services) {
            try {
                serviceManager.registerService(service);
            } catch (Exception e) {
                e.printStackTrace();
                this.getPackage().getLogger().severe("failed to register service %s: %s".formatted(
                        service.getName(), e.getMessage()));
                throw new RuntimeException(e);
            }
        }
    }

    public final void unregister(ServiceManager serviceManager) {
        for (Class<? extends Service> service : this.services) {
            try {
                serviceManager.unregisterService(service);
            } catch (Exception e) {
                this.getPackage().getLogger().severe("failed to unregister service %s: %s".formatted(
                        service.getName(), e.getMessage()));
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

    public abstract void create(Set<Class<? extends Service>> services);
}
