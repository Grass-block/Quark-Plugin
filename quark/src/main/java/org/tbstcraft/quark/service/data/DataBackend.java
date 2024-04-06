package org.tbstcraft.quark.service.data;

import me.gb2022.commons.nbt.NBTTagCompound;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class DataBackend {
    private final File folder;
    private boolean open;

    protected DataBackend(File folder) {
        this.folder = folder;
    }

    public final File getFolder() {
        return folder;
    }

    public final boolean isOpen() {
        return this.open;
    }

    public final void open() {
        this.onOpen();
        this.open = true;
    }

    public final void close() {
        this.open = false;
        this.onClose();
    }

    public void onOpen() {
    }

    public void onClose() {
    }


    public abstract int count();

    public abstract NBTTagCompound load(String entryId);

    public abstract void save(String entryId, NBTTagCompound tag);

    public abstract void foreach(BiConsumer<String, NBTTagCompound> function);
}
