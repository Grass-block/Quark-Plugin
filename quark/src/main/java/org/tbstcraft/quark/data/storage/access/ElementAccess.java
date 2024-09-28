package org.tbstcraft.quark.data.storage.access;

public abstract class ElementAccess<I,H> implements StorageAccess<I,H> {
    protected final String name;

    protected ElementAccess(String name) {
        this.name = name;
    }


}