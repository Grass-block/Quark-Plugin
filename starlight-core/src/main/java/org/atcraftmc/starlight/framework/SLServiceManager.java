package org.atcraftmc.starlight.framework;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.service.ServiceManager;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.injection.ServiceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public final class SLServiceManager extends ServiceManager<SLService> {
    public static final Logger LOGGER = LogManager.getLogger("SLServiceManager");
    public static final SLServiceManager INSTANCE = new SLServiceManager();
    private final HashMap<String, Class<? extends SLService>> services = new HashMap<>(24);

    static <I extends SLService> Class<I> get(String id, Class<Class<I>> type) {
        return INSTANCE.getService(id, type);
    }

    static <I extends SLService> void register(Class<I> service) {
        INSTANCE.registerService(service);
    }

    static void unregister(Class<? extends SLService> service) {
        INSTANCE.unregisterService(service);
    }

    public static void unregisterAll(ServiceLayer layer) {
        INSTANCE.unregisterAllServices(layer);
    }

    static <T extends SLService> T createImplementation(Class<T> clazz, ConfigEntry config) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) == null) {
                continue;
            }

            try {
                if (m.getParameterTypes().length == 0) {
                    return clazz.cast(m.invoke(null));
                }

                return clazz.cast(m.invoke(null, config));
            } catch (NoClassDefFoundError ignored) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (e.getCause() instanceof APIIncompatibleException) {
                    return null;
                }
                if (e.getCause() instanceof NoClassDefFoundError) {
                    return null;
                }

                throw new RuntimeException(e);
            }
        }

        var implClass = clazz.getAnnotation(ApplicationService.class).impl();

        if (implClass == SLService.class) {
            return null;
        }

        try {
            return clazz.cast(implClass.getDeclaredConstructor(ConfigEntry.class).newInstance(config));
        } catch (NoSuchMethodException e) {
            try {
                return clazz.cast(implClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                if (e.getCause() instanceof APIIncompatibleException) {
                    return null;
                }
                if (e.getCause() instanceof NoClassDefFoundError) {
                    return null;
                }
                throw new RuntimeException(ex);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            if (ex.getCause() instanceof APIIncompatibleException) {
                return null;
            }
            if (ex.getCause() instanceof NoClassDefFoundError) {
                return null;
            }
            throw new RuntimeException(ex);
        }
    }

    public static HashMap<String, Class<? extends SLService>> all() {
        return (INSTANCE).services;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public <E extends SLService> void exportService(E object, Class<E> type) {
        Bukkit.getServicesManager().register(type, object, Starlight.instance(), ServicePriority.High);
    }

    @Override
    public void handleException(Throwable e) {
        LOGGER.catching(e);
    }

    @Override
    public void unregisterExportedService(Class<? extends SLService> type) {
        Bukkit.getServicesManager().unregister(type, Starlight.instance());
    }

    @Override
    public <E extends SLService> E createImplementation(Class<E> service, String id) {
        return SLServiceManager.createImplementation(service, ConfigContainer.getInstance().entry("starlight-core", id));
    }
}
