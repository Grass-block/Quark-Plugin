package org.tbstcraft.quark.data.storage;

import me.gb2022.commons.nbt.NBTTagCompound;

public final class DataEntry extends StorageTable {
    private final String id;

    public DataEntry(NBTTagCompound tag, StorageContext context, String id) {
        super(tag, context,null);
        this.id = id;
    }

    public DataEntry(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public void save() {
        this.getContext().save(this);
    }

    @Override
    public DataEntry getRoot() {
        return this;
    }
}
