package org.atcraftmc.starlight.foundation.platform;

import me.gb2022.modular.APIIncompatibleException;
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
            throw new APIIncompatibleException("Missing class: " + e.getMessage());
        }
    }

    static void requireMethod(MethodAssertion supplier) {
        try {
            supplier.get();
        } catch (Throwable e) {
            throw new APIIncompatibleException("Missing method: " + e.getMessage());
        }
    }

    static void blackListPlatform(APIProfile... platform) {
        for (APIProfile profile : platform) {
            if (APIProfileTest.getAPIProfile() == profile) {
                throw new APIIncompatibleException("Unsupported platform: " + profile.name);
            }
        }
    }

    static void requirePlugin(String name) {
        if (Bukkit.getPluginManager().getPlugin(name) == null) {
            throw new APIIncompatibleException("Plugin not found: " + name);
        }
    }

    static void assertion(boolean complete) {
        if (!complete) {
            throw new APIIncompatibleException("Assertion failed");
        }
    }

    interface MethodAssertion {
        Method get() throws NoSuchMethodException;
    }

    interface ClassAssertion {
        Class<?> get() throws ClassNotFoundException;
    }



}
