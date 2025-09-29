package org.atcraftmc.starlight.internal;

import me.gb2022.modular.service.ApplicationService;
import org.atcraftmc.starlight.framework.SLService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.starlight.util.CachedInfo;

import java.util.HashMap;
import java.util.Map;

@ApplicationService(id = "cache", impl = CacheService.Impl.class)
public interface CacheService extends SLService {

    @ServiceInject
    ServiceHolder<CacheService> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void start() {
        CachedInfo.init();
    }

    @ServiceInject
    static void stop() {
        CachedInfo.stop();
    }

    static <I> void setItem(String id, I item) {
        INSTANCE.get().set(id, item);
    }

    static <I> I getItem(String id, Class<I> clazz) {
        return INSTANCE.get().get(id, clazz);
    }

    <I> void set(String id, I item);

    <I> I get(String id, Class<I> clazz);

    final class Impl implements CacheService {
        private final Map<String, Object> map = new HashMap<>();

        @Override
        public <I> void set(String id, I item) {
            this.map.put(id, item);
        }

        @Override
        public <I> I get(String id, Class<I> clazz) {
            return clazz.cast(this.map.get(id));
        }
    }
}
