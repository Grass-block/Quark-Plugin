package org.tbstcraft.quark.internal;

import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.container.CachedInfo;

import java.util.HashMap;
import java.util.Map;

@QuarkService(id = "cache", impl = CacheService.Impl.class)
public interface CacheService extends Service {

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
