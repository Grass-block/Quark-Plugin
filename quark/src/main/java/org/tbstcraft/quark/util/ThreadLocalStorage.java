package org.tbstcraft.quark.util;

import java.util.HashMap;
import java.util.function.Supplier;

public class ThreadLocalStorage<V> {
    private final HashMap<Thread, V> map = new HashMap<>();
    private final Supplier<V> supplier;

    public ThreadLocalStorage(Supplier<V> supplier) {
        this.supplier = supplier;
    }

    public V get() {
        Thread t = Thread.currentThread();
        if (!this.map.containsKey(t)) {
            this.map.put(t,this.supplier.get());
        }
        return map.get(t);
    }
}
