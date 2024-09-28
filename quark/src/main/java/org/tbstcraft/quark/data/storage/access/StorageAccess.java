package org.tbstcraft.quark.data.storage.access;

public interface StorageAccess<I, H> {
    void set(H holder, I value);

    I get(H value);

    void save();

    boolean contains(H holder);

    default void setAndSave(H holder, I value) {
        set(holder, value);
        save();
    }
}
