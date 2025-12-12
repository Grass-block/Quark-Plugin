package org.atcraftmc.starlight.framework;

import me.gb2022.modular.service.Service;
import org.atcraftmc.starlight.core.JDBCService;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.permission.PermissionService;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.data.PlayerDataService;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.atcraftmc.starlight.internal.ProductService;
import org.bukkit.event.Listener;

@SuppressWarnings({"rawtypes"})
public interface SLService extends Service, Listener {
    Class[] BASE_SERVICES = new Class[]{
            JDBCService.class,
            PermissionService.class,
            ProductService.class,
            SLPackageManager.class,
            SLModuleManager.class,
            LocaleService.class,
            PlayerDataService.class,
            ModuleDataService.class,
    };


    static void initBase() {
        for (Class<? extends SLService> clazz : BASE_SERVICES) {
            SLServiceManager.register(clazz);
        }
    }

    @Override
    default void enable() throws Exception {
        Service.super.enable();
    }

    @Override
    default void disable() throws Exception {
        Service.super.disable();
    }
}
