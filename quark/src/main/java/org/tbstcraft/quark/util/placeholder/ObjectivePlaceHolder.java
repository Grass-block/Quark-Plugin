package org.tbstcraft.quark.util.placeholder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ObjectivePlaceHolder<I> {
    private final Map<String, ObjectPlaceHolder<I>> map = new HashMap<>();
    private final Set<String> registerKeys = new HashSet<>();

    public void register(String path, ObjectPlaceHolder<I> getter) {
        registerKeys.add(path);
        map.put(path, getter);
    }

    public void unregister(String path) {
        registerKeys.remove(path);
        map.remove(path);
    }

    public Set<String> getRegisterKeys() {
        return registerKeys;
    }

    public String get(String path, I target) {
        if (!this.has(path)) {
            return GloballyPlaceHolder.defaultValue(path);
        }
        return this.map.get(path).getText(target);
    }

    public ComponentLike getComponent(String path, I target) {
        if (!this.has(path)) {
            return Component.text(GloballyPlaceHolder.defaultValue(path));
        }
        return this.map.get(path).get(target);
    }

    public void append(ObjectivePlaceHolder<I> placeHolder) {
        this.map.putAll(placeHolder.map);
    }

    public boolean has(String path) {
        return this.map.containsKey(path);
    }
}
