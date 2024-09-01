package org.tbstcraft.quark.framework.service;

import me.gb2022.commons.reflect.Annotations;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public interface ServiceManager {
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

    static <T extends Service> T createImplementation(Class<T> clazz, ConfigEntry config) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) == null) {
                continue;
            }
            try {
                return clazz.cast(m.invoke(null, config));
            } catch (IllegalAccessException | InvocationTargetException e) {
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
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    static boolean hasImplementation(Class<? extends Service> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) != null) {
                return true;
            }
        }

        Class<? extends Service> implClass = clazz.getAnnotation(QuarkService.class).impl();

        return implClass != Service.class;
    }

    <I extends Service> Class<I> getService(String id, Class<Class<I>> type);

    <I extends Service> void registerService(Class<I> service);

    void unregisterService(Class<? extends Service> service);

    void unregisterAllServices();

    final class Impl implements ServiceManager {
        private final HashMap<String, Class<? extends Service>> services = new HashMap<>(16);

        @Override
        public <I extends Service> Class<I> getService(String id, Class<Class<I>> type) {
            return type.cast(this.services.get(id));
        }


        @Override
        @SuppressWarnings("unchecked")
        public <I extends Service> void registerService(Class<I> service) {
            String id = Service.getServiceId(service);
            if (this.services.containsKey(id)) {
                throw new RuntimeException("exist registered service: %s".formatted(id));
            }
            this.services.put(id, service);


            String sid = service.getAnnotation(QuarkService.class).id();


            if (hasImplementation(service)) {
                for (Field f : service.getFields()) {
                    if (f.getAnnotation(ServiceInject.class) == null) {
                        continue;
                    }

                    f.setAccessible(true);

                    try {
                        ServiceHolder<Service> holder = ((ServiceHolder<Service>) f.get(null));

                        I instance = createImplementation(service, ConfigContainer.getInstance().entry("quark-core", sid));

                        holder.set(instance);

                        assert instance != null;

                        if (Annotations.hasAnnotation(holder, RegisterAsGlobal.class)) {
                            Bukkit.getServicesManager().register(service, instance, Quark.getInstance(), ServicePriority.High);
                        }


                        holder.get().onEnable();
                    } catch (Throwable e) {
                        Quark.getInstance().getLogger().severe("failed to set implementation for service [%s]".formatted(id));
                        ExceptionUtil.log(e);
                    }
                }
            }

            try {
                Method m = service.getMethod("start");

                if (m.getAnnotation(ServiceInject.class) == null) {
                    return;
                }

                m.invoke(null);

            } catch (NoSuchMethodException ignored) {
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void unregisterService(Class<? extends Service> service) {
            String id = Service.getServiceId(service);
            this.services.remove(id);

            try {
                Method m = service.getMethod("stop");

                if (m.getAnnotation(ServiceInject.class) == null) {
                    return;
                }

                m.invoke(null);

            } catch (NoSuchMethodException ignored) {
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            for (Field f : service.getFields()) {
                if (f.getAnnotation(ServiceInject.class) == null) {
                    continue;
                }

                f.setAccessible(true);

                try {
                    ServiceHolder<Service> holder = ((ServiceHolder<Service>) f.get(null));

                    if (holder.get() == null) {
                        continue;
                    }

                    if (Annotations.hasAnnotation(holder, RegisterAsGlobal.class)) {
                        Bukkit.getServicesManager().unregister(service);
                    }

                    holder.get().onDisable();
                } catch (Throwable e) {
                    Quark.getInstance().getLogger().severe("failed to stop implementation for [%s]".formatted(id));
                    ExceptionUtil.log(e);
                }
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
