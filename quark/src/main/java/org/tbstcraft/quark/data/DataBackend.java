package org.tbstcraft.quark.data;

import org.tbstcraft.quark.util.nbt.NBTTagCompound;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DataBackend {
    private final HashMap<String, NBTTagCompound> cache = new HashMap<>();
    private final File folder;

    protected DataBackend(File folder) {
        this.folder = folder;
    }

    public File getFolder() {
        return folder;
    }

    public void open() {
    }

    public final NBTTagCompound getEntry(String id) {
        if (this.cache.containsKey(id)) {
            return this.cache.get(id);
        }
        NBTTagCompound tag = this.loadEntry(id);
        this.cache.put(id, tag);
        return tag;
    }

    public void save(String id) {
        NBTTagCompound tag = this.cache.get(id);
        this.cache.remove(id);
        this.saveEntry(id, tag);
    }

    public void close() {
        for (String s : new ArrayList<>(this.cache.keySet())) {
            save(s);
        }
    }

    public abstract NBTTagCompound loadEntry(String id);

    public abstract void saveEntry(String id, NBTTagCompound tag);
}
