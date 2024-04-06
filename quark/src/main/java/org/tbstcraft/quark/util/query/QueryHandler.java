package org.tbstcraft.quark.util.query;

import java.util.HashMap;
import java.util.function.Supplier;

public class QueryHandler {
    private final HashMap<String, Supplier<Object>> suppliers = new HashMap<>();

    public void register(String id, Supplier<Object> supplier) {
        this.suppliers.put(id, supplier);
    }

    public void unregister(String id) {
        this.suppliers.remove(id);
    }

    public String query(String key) {
        Supplier<Object> supplier = this.suppliers.get(key);
        if (supplier == null) {
            return null;
        }
        return supplier.get().toString();
    }
}
