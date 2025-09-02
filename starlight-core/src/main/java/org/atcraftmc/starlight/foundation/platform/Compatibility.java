package org.atcraftmc.starlight.foundation.platform;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;

public interface Compatibility {

    static void reversed(String msg, Runnable action) {
        try {
            action.run();
        } catch (APIIncompatibleException ignored) {
            return;
        }

        throw new APIIncompatibleException(msg);
    }

    static void requirePDC() {
        requireClass(() -> Class.forName("org.bukkit.persistence.PersistentDataHolder"));
    }

    static void requireClass(ClassAssertion supplier) {
        try {
            supplier.get();
        } catch (Throwable e) {
            throw new APIIncompatibleException(e.getMessage());
        }
    }

    static void requireMethod(MethodAssertion supplier) {
        try {
            supplier.get();
        } catch (Throwable e) {
            throw new APIIncompatibleException(e.getMessage());
        }
    }

    static void blackListPlatform(APIProfile... platform) {
        for (APIProfile profile : platform) {
            if (APIProfileTest.getAPIProfile() == profile) {
                throw new APIIncompatibleException("unsupported platform: " + profile.name);
            }
        }
    }

    static void requirePlugin(String name) {
        if (Bukkit.getPluginManager().getPlugin(name) == null) {
            throw new APIIncompatibleException("plugin not found: " + name);
        }
    }

    static void assertion(boolean complete) {
        if (!complete) {
            throw new APIIncompatibleException("assertion failed");
        }
    }

    interface MethodAssertion {
        Method get() throws NoSuchMethodException;
    }

    interface ClassAssertion {
        Class<?> get() throws ClassNotFoundException;
    }



}
