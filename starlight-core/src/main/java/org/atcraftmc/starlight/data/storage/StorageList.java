package org.atcraftmc.starlight.data.storage;

import me.gb2022.commons.nbt.NBTTagList;

public final class StorageList extends NBTTagList implements DataReferenced {
    private DataReference reference;

    public StorageList(NBTTagList dom, DataReference reference) {
        this.reference = reference;
        this.getTagList().clear();
        this.getTagList().addAll(dom.getTagList());

    }

    public StorageList() {
        this(new NBTTagList(), null);
    }


    @Override
    public void setReference(DataReference reference) {
        this.reference = reference;
    }

    @Override
    public DataReference getReference() {
        return reference;
    }

    public StorageTable getStorageTable(int position) {
        return new StorageTable(getCompoundTag(position), this.reference);
    }

    public void setStorageTable(int position, StorageTable table) {
        table.setReference(this.reference);
        setTag(position, table);
    }

    public void addStorageTable(StorageTable table) {
        table.setReference(this.reference);
        addTag(table);
    }

    public StorageList getStorageList(int position) {
        return new StorageList(getTagList(position), this.reference);
    }

    public void setStorageList(int position, StorageList list) {
        list.setReference(this.reference);
        setTag(position, list);
    }

    public void addStorageList(StorageList list) {
        list.setReference(this.reference);
        addTag(list);
    }
}