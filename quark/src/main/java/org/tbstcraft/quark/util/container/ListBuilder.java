package org.tbstcraft.quark.util.container;

import java.util.List;

public class ListBuilder<V> {
    private final List<V> list;

    public ListBuilder(List<V> list) {
        this.list = list;
    }

    static <V> ListBuilder<V> of(List<V> map) {
        return new ListBuilder<>(map);
    }

    public ListBuilder<V> add(V v) {
        this.list.add(v);
        return this;
    }

    public ListBuilder<V> set(int pos,V v) {
        this.list.add(v);
        return this;
    }

    public ListBuilder<V> delete(V v) {
        this.list.remove(v);
        return this;
    }

    public ListBuilder<V> clear() {
        this.list.clear();
        return this;
    }
}
