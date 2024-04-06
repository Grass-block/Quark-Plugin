package org.tbstcraft.quark.util.query;

import com.google.common.base.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public final class ValueSupplier implements Supplier<Object> {
    private final Object object;

    public ValueSupplier(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public Object get() {
        return this.getObject();
    }
}
