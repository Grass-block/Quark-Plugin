package org.tbstcraft.quark.util;

public interface CompatibleAssertion {
    static void existClass(String name) {
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static void existMethod(Class<?> target, String name, Class<?>... args) {
        try {
            target.getMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
