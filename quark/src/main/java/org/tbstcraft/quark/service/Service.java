package org.tbstcraft.quark.service;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.framework.PackageManager;
import org.tbstcraft.quark.service.framework.ProductService;
import org.tbstcraft.quark.service.proxy.ProxyMessageService;
import org.tbstcraft.quark.service.audience.AudienceService;
import org.tbstcraft.quark.service.data.ModuleDataService;
import org.tbstcraft.quark.service.data.PlayerDataService;
import org.tbstcraft.quark.service.framework.ModuleManager;
import org.tbstcraft.quark.service.record.RecordService;
import org.tbstcraft.quark.service.task.TaskService;
import org.tbstcraft.quark.service.ui.UIManager;
import org.tbstcraft.quark.service.web.HTTPService;
import org.tbstcraft.quark.service.web.SMTPMailService;
import org.tbstcraft.quark.service.web.TokenStorage;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.lang.reflect.Field;

@SuppressWarnings("rawtypes")
public interface Service {
    Class[] SERVICES = new Class[]{
            CommandEventService.class,
            PlayerAuthService.class,
            RecordService.class,
            SMTPMailService.class,
            HTTPService.class,
            WESessionTrackService.class,
            PermissionService.class,
            UIManager.class,
            ProxyMessageService.class,
            CipherService.class,
            //ProtocolLibService.class
    };

    Class[] BASE_SERVICES = new Class[]{
            ProductService.class,
            TaskService.class,
            AudienceService.class,

            CacheService.class,

            ModuleDataService.class,
            PlayerDataService.class,
            PackageManager.class,
            ModuleManager.class
    };

    static void initBase() {
        for (Class<?> clazz : BASE_SERVICES) {
            startService(clazz);
        }
    }

    static void stopBase() {
        for (int i = BASE_SERVICES.length - 1; i > 0; i--) {
            stopService(BASE_SERVICES[i]);
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

    default void onEnable() {
    }

    default void onDisable() {
    }
}
