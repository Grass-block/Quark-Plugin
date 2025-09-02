package org.atcraftmc.starlight.data.storage.access;

import org.bukkit.entity.Player;

public interface StorageAccess<I, H> {
    void set(H holder, I value);

    I get(H value);

    void save(H holder);

    boolean contains(H holder);

    default void setAndSave(H holder, I value) {
        set(holder, value);
        save(holder);
    }
}
