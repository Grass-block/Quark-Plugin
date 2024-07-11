package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GloballyPlaceHolder {
    private final Map<String, GlobalPlaceHolder> map = new HashMap<>();
    private final Set<String> registerKeys = new HashSet<>();

    public static String defaultValue(String path) {
        return "%" + path + "%";
    }

    public void register(String path, GlobalPlaceHolder getter, String... aliases) {
        registerKeys.add(path);
        map.put(path, getter);
        for (String alias : aliases) {
            map.put(alias, getter);
        }
    }

    public void register(String path, Object value, String... aliases) {
        register(path, GlobalPlaceHolder.value(value), aliases);
    }

    public void unregister(String path) {
        registerKeys.remove(path);
        map.remove(path);
    }

    public Set<String> getRegisterKeys() {
        return registerKeys;
    }

    public boolean has(String path) {
        return this.map.containsKey(path);
    }

    public String get(String path) {
        if (!this.has(path)) {
            return defaultValue(path);
        }
        return this.map.get(path).getText();
    }

    public ComponentLike getComponent(String path) {
        if (!this.has(path)) {
            return Component.text(defaultValue(path));
        }
        return this.map.get(path).get();
    }

    public void append(GloballyPlaceHolder placeHolder) {
        this.map.putAll(placeHolder.map);
    }

    public void clear() {
        this.map.clear();
    }
}
