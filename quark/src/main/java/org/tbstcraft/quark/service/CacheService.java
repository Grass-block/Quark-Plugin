package org.tbstcraft.quark.service;

import org.tbstcraft.quark.util.container.CachedInfo;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.util.HashMap;
import java.util.Map;


@ServiceImplementation(impl = CacheService.Impl.class)
public interface CacheService extends Service {
    ObjectContainer<CacheService> INSTANCE = new ObjectContainer<>();

    static void init() {
        CachedInfo.init();
    }

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
