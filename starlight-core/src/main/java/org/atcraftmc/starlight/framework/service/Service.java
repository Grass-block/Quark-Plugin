package org.atcraftmc.starlight.framework.service;

import org.atcraftmc.starlight.core.JDBCService;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.permission.PermissionService;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.framework.FunctionalComponent;
import org.atcraftmc.starlight.framework.module.ModuleManager;
import org.atcraftmc.starlight.framework.packages.PackageManager;
import org.atcraftmc.starlight.internal.ProductService;

@SuppressWarnings({"rawtypes"})
public interface Service extends FunctionalComponent {
    Class[] BASE_SERVICES = new Class[]{
            JDBCService.class,
            PermissionService.class,
            ProductService.class,
            PackageManager.class,
            ModuleManager.class,
            LocaleService.class,
            PlayerDataService.class,
            ModuleDataService.class,
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
        return clazz.getAnnotation(SLService.class).id();
    }

    static ServiceLayer getServiceLayer(Class<? extends Service> clazz) {
        return clazz.getAnnotation(SLService.class).layer();
    }


    default void onEnable() {
        this.enable();
    }

    default void onDisable() {
        this.disable();
    }
}
