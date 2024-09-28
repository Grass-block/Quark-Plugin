package org.tbstcraft.quark.framework.service;

import org.tbstcraft.quark.framework.module.FunctionalComponent;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.internal.LocaleService;
import org.tbstcraft.quark.internal.ProductService;
import org.tbstcraft.quark.internal.permission.PermissionService;

@SuppressWarnings({"rawtypes"})
public interface Service extends FunctionalComponent {
    Class[] BASE_SERVICES = new Class[]{
            PermissionService.class,
            ProductService.class,
            PackageManager.class,
            ModuleManager.class,
            LocaleService.class
    };

    static void initBase() {
        for (Class<? extends Service> clazz : BASE_SERVICES) {
            ServiceManager.register(clazz);
        }
    }

    static void stopBase() {
        for (int i = BASE_SERVICES.length - 1; i > 0; i--) {
            ServiceManager.unregister(BASE_SERVICES[i]);
        }
    }

    static String getServiceId(Class<? extends Service> clazz) {
        return clazz.getAnnotation(QuarkService.class).id();
    }


    default void onEnable() {
        this.enable();
    }

    default void onDisable() {
        this.disable();
    }
}
