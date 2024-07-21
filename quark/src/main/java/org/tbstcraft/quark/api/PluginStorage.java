package org.tbstcraft.quark.api;

import org.tbstcraft.quark.Quark;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public interface PluginStorage {
    Map<String, Object> STORAGE = new HashMap<>();

    static <I> Set<I> set(String name, Consumer<HashSet<I>> func) {
        return object(name, HashSet.class, func::accept);
    }

    static <I> List<I> list(String name, Consumer<List<I>> func) {
        return object(name, List.class, func::accept);
    }

    static <I, I2> Map<I, I2> map(String name, Consumer<Map<I, I2>> func) {
        return object(name, Map.class, func::accept);
    }

    static <I> I object(String name, Class<I> type, Consumer<I> func) {
        I item = type.cast(STORAGE.get(name));

        if (item == null) {
            try {
                register(name, type.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                Quark.LOGGER.warning("cannot create instance of " + type.getSimpleName());
            }
        }

        if (item != null) {
            func.accept(item);
        }

        return item;
    }

    static <I> I register(String name, I item) {
        STORAGE.put(name, item);
        return item;
    }
}
