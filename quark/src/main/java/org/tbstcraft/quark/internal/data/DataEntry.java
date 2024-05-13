package org.tbstcraft.quark.internal.data;

import me.gb2022.commons.nbt.NBTTagCompound;

public class DataEntry extends NBTTagCompound {
    private String id;
    private DataBackend supplier;

    public void init(String id, DataBackend supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    public void save() {
        this.supplier.save(this.id, this);
    }

}
