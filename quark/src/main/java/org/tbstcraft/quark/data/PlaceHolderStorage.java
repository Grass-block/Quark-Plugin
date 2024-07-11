package org.tbstcraft.quark.data;

import org.tbstcraft.quark.Quark;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public interface PlaceHolderStorage {
    Map<String, Object> STORAGE = new HashMap<>();

    static <I> I get(String name, Class<I> type, Consumer<I> func) {
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
