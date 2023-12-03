package org.tbstcraft.quark.util.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class OrderedHashMap<K, V> extends HashMap<K, V> {
    private final List<K> order = new ArrayList<>();


    @Override
    public V put(K key, V value) {
        if (!order.contains(key)) {
            order.add(key);
        }
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        order.remove(key);
        return super.remove(key);
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (K k : this.order) {
            values.add(this.get(k));
        }
        return values;
    }
}
