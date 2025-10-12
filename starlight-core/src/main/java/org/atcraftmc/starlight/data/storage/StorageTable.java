package org.atcraftmc.starlight.data.storage;

import me.gb2022.commons.nbt.NBTTagCompound;

public class StorageTable extends NBTTagCompound implements DataReferenced {
    private DataReference reference;

    public StorageTable(NBTTagCompound dom, DataReference reference) {
        this.getTagMap().clear();
        this.getTagMap().putAll(dom.getTagMap());

        this.reference = reference;
    }

    public StorageTable() {
        this(new NBTTagCompound(), null);
    }

    @Override
    public void save() {
        this.reference.save();
    }

    @Override
    public DataReference getReference() {
        return reference;
    }

    @Override
    public void setReference(DataReference reference) {
        this.reference = reference;
    }

    public StorageTable getTable(String id) {
        return new StorageTable(getCompoundTag(id), this.reference);
    }

    public void setTable(String id, StorageTable table) {
        table.setReference(this.reference);
        setTag(id, table);
    }

    public StorageList getList(String id) {
        return new StorageList(getTagList(id), this.reference);
    }

    public void setList(String id, StorageList list) {
        list.setReference(this.reference);
        setTag(id, list);
    }
}
