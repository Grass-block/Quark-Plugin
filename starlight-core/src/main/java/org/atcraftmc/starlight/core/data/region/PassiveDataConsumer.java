package org.atcraftmc.starlight.core.data.region;

public interface PassiveDataConsumer<T> {
    void loadData(String key, T value);

    void unload(String key);
}
