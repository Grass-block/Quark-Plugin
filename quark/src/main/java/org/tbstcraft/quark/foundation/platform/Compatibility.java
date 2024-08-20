package org.tbstcraft.quark.foundation.platform;

import java.lang.reflect.Method;

public interface Compatibility {

    static void requirePDC() {
        requireClass(() -> Class.forName("org.bukkit.persistence.PersistentDataHolder"));
    }

    static void requireClass(ClassAssertion supplier) {
        try {
            supplier.get();
        } catch (ClassNotFoundException e) {
            throw new APIIncompatibleException(e.getMessage());
        }
    }

    static void requireMethod(MethodAssertion supplier) {
        try {
            supplier.get();
        } catch (NoSuchMethodException e) {
            throw new APIIncompatibleException(e.getMessage());
        }
    }

    interface MethodAssertion {
        Method get() throws NoSuchMethodException;
    }

    interface ClassAssertion {
        Class<?> get() throws ClassNotFoundException;
    }
}
