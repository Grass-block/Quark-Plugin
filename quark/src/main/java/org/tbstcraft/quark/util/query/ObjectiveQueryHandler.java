package org.tbstcraft.quark.util.query;

import java.util.HashMap;
import java.util.function.Function;

public final class ObjectiveQueryHandler<T> {
    private final HashMap<String, Function<T, String>> suppliers = new HashMap<>();

    public void register(String id, Function<T, String> supplier) {
        this.suppliers.put(id, supplier);
    }

    public void unregister(String id) {
        this.suppliers.remove(id);
    }

    public String query(T obj, String key) {
        Function<T, String> supplier = this.suppliers.get(key);
        if (supplier == null) {
            return null;
        }
        return supplier.apply(obj);
    }
}
