package org.tbstcraft.quark.framework.service;

import me.gb2022.commons.reflect.Annotations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public interface ServiceManager {
    Logger LOGGER = LogManager.getLogger("Quark/ServiceManager");

    ServiceManager INSTANCE = new Impl();

    static <I extends Service> Class<I> get(String id, Class<Class<I>> type) {
        return INSTANCE.getService(id, type);
    }

    static <I extends Service> void register(Class<I> service) {
        INSTANCE.registerService(service);
    }

    static void unregister(Class<? extends Service> service) {
        INSTANCE.unregisterService(service);
    }

    static void unregisterAll() {
        INSTANCE.unregisterAllServices();
    }

    static boolean hasImplementation(Class<? extends Service> clazz) {
        for (var m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) != null) {
                return true;
            }
        }

        var implClass = clazz.getAnnotation(QuarkService.class).impl();

        return implClass != Service.class;
    }

    static boolean isLazy(Class<? extends Service> clazz) {
        return clazz.getAnnotation(QuarkService.class).requiredBy().length != 0;
    }

    static <T extends Service> T createImplementation(Class<T> clazz, ConfigEntry config) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) == null) {
                continue;
            }

            try {
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

        Class<? extends Service> implClass = clazz.getAnnotation(QuarkService.class).impl();

        if (implClass == Service.class) {
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


    static HashMap<String, Class<? extends Service>> all() {
        return ((Impl) INSTANCE).services;
    }

    <I extends Service> Class<I> getService(String id, Class<Class<I>> type);

    <I extends Service> void registerService(Class<I> service);

    void unregisterService(Class<? extends Service> service);

    void unregisterAllServices();

    final class Impl implements ServiceManager {
        private final HashMap<String, Class<? extends Service>> services = new HashMap<>(16);

        private Field getInjection(Class<? extends Service> service) {
            if (!hasImplementation(service)) {
                return null;
            }

            Field injection = null;

            for (var f : service.getDeclaredFields()) {
                if (f.getAnnotation(ServiceInject.class) == null) {
                    continue;
                }

                if (injection == null) {
                    injection = f;
                } else {
                    throw new IllegalArgumentException("find multiple injection point in %s, this will cause BUGS!".formatted(service));
                }
            }

            if (injection != null) {
                injection.setAccessible(true);
            }

            return injection;
        }

        private <I extends Service> void inject(Class<I> service, String id, Field inject) {
            try {
                var holder = ((ServiceHolder<Service>) inject.get(null));
                var instance = createImplementation(service, ConfigContainer.getInstance().entry("quark-core", id));
                assert instance != null;

                holder.set(instance);

                try {
                    holder.get().checkCompatibility();
                } catch (APIIncompatibleException e) {
                    LOGGER.warn("service {} failed compat check: {}", id, e.getCause().toString());
                    holder.set(null);
                    return;
                }

                if (Annotations.hasAnnotation(holder, RegisterAsGlobal.class)) {
                    Bukkit.getServicesManager().register(service, instance, Quark.getInstance(), ServicePriority.High);
                }

                holder.get().onEnable();
            } catch (Throwable e) {
                LOGGER.error("failed to set implementation for service [{}]:", id);
                ExceptionUtil.log(e);
            }
        }

        @Override
        public <I extends Service> Class<I> getService(String id, Class<Class<I>> type) {
            return type.cast(this.services.get(id));
        }

        @Override
        public <I extends Service> void registerService(Class<I> service) {
            var id = Service.getServiceId(service);

            if (this.services.containsKey(id)) {
                throw new RuntimeException("exist registered service: %s".formatted(id));
            }
            this.services.put(id, service);

            var inject = getInjection(service);

            if (inject != null) {
                inject(service, id, inject);
            }

            try {
                Method m = service.getMethod("start");

                if (m.getAnnotation(ServiceInject.class) == null) {
                    return;
                }

                try {
                    m.invoke(null);
                } catch (Throwable e) {
                    ExceptionUtil.log(e);
                }

            } catch (NoSuchMethodException ignored) {
            }
        }

        @Override
        public void unregisterService(Class<? extends Service> service) {
            String id = Service.getServiceId(service);
            this.services.remove(id);

            try {
                Method m = service.getMethod("stop");

                if (m.getAnnotation(ServiceInject.class) != null) {
                    m.invoke(null);
                }
            } catch (NoSuchMethodException ignored) {
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var inject = getInjection(service);

            if (inject == null) {
                return;
            }

            try {
                var handle = service.cast(((ServiceHolder<?>) inject.get(null)).get());

                if (handle == null) {
                    return;
                }

                if (Annotations.hasAnnotation(inject, RegisterAsGlobal.class)) {
                    Bukkit.getServicesManager().unregister(service);
                }

                handle.onDisable();
            } catch (Throwable e) {
                LOGGER.error("failed to stop implementation for [{}]", id);
                ExceptionUtil.log(e);
            }
        }

        @Override
        public void unregisterAllServices() {
            for (Class<? extends Service> serviceClass : new HashSet<>(this.services.values())) {
                this.unregisterService(serviceClass);
            }
        }
    }
}
