package org.tbstcraft.quark.framework.service;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.service.base.ProductService;
import org.tbstcraft.quark.service.base.permission.PermissionService;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.service.CipherService;
import org.tbstcraft.quark.service.PlayerAuthService;
import org.tbstcraft.quark.service.ServiceImplementation;
import org.tbstcraft.quark.service.WESessionTrackService;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.service.ui.UIManager;
import org.tbstcraft.quark.service.network.HttpService;
import org.tbstcraft.quark.service.network.SMTPService;
import org.tbstcraft.quark.service.network.http.TokenStorage;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.lang.reflect.Field;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface Service {
    Class[] SERVICES = new Class[]{
            PlayerAuthService.class,
            SMTPService.class,
            HttpService.class,
            WESessionTrackService.class,
            PermissionService.class,
            UIManager.class,
            ProxyMessageService.class,
            CipherService.class,
    };

    Class[] BASE_SERVICES = new Class[]{
        ProductService.class,
        PackageManager.class,
        ModuleManager.class,
        PermissionService.class,
        TaskService.class
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

    @SuppressWarnings("unchecked")
    private static void stopService(Class<?> clazz) {
        try {
            try {
                clazz.getMethod("stop").invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
            ServiceImplementation container = clazz.getDeclaredAnnotation(ServiceImplementation.class);
            if (container != null) {
                Field f = clazz.getField("INSTANCE");
                f.setAccessible(true);
                ObjectContainer<Service> target = (ObjectContainer<Service>) f.get(null);
                target.get().onDisable();
            }
        } catch (Throwable e) {
            Quark.LOGGER.severe("failed to restart service %s : %s".formatted(clazz.getName(), ExceptionUtil.getMessage(e)));
            ExceptionUtil.log(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void startService(Class<?> clazz) {
        try {
            ServiceImplementation container = clazz.getDeclaredAnnotation(ServiceImplementation.class);
            if (container != null) {
                Field f = clazz.getField("INSTANCE");
                f.setAccessible(true);
                ObjectContainer<Service> target = (ObjectContainer<Service>) f.get(null);
                target.set(container.impl().getDeclaredConstructor().newInstance());
                target.get().onEnable();
            }
            try {
                clazz.getMethod("init").invoke(null);
            } catch (NoSuchMethodException ignored) {
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Quark.LOGGER.severe("failed to restart service %s : %s".formatted(clazz.getName(), ExceptionUtil.getMessage(e)));
        }
    }

    static void init() {
        for (Class<?> clazz : SERVICES) {
            startService(clazz);
        }
        TaskService.asyncTimerTask("quark_core:web:token_update", 0, 20, TokenStorage.UPDATE_TASK);
    }

    static void stop() {
        for (int i = SERVICES.length - 1; i > 0; i--) {
            stopService(SERVICES[i]);
        }
        TaskService.cancelTask("quark_core:web:token_update");
    }

    static String getServiceId(Class<? extends Service> clazz) {
        return clazz.getAnnotation(QuarkService.class).id();
    }

    default void onEnable() {
    }

    default void onDisable() {
    }
}
