package org.tbstcraft.quark.util.container;

import java.util.Map;

public class MapBuilder<K, V> {
    private final Map<K, V> map;

    public MapBuilder(Map<K, V> map) {
        this.map = map;
    }

    static <K, V> MapBuilder<K, V> of(Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    public MapBuilder<K, V> put(K k, V v) {
        this.map.put(k, v);
        return this;
    }

    public MapBuilder<K, V> delete(K k) {
        this.map.remove(k);
        return this;
    }

    public MapBuilder<K, V> clear() {
        this.map.clear();
        return this;
    }
}
