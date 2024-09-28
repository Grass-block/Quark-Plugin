package org.tbstcraft.quark.data.storage;

import me.gb2022.commons.nbt.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

public final class StorageList extends NBTTagList {
    final NBTTagList tag;
    final DataEntry root;
    private final StorageContext context;

    public StorageList(NBTTagList tag, StorageContext context, DataEntry root) {
        this.context = context;
        this.tag = tag;
        this.root = root;
    }

    public StorageList() {
        this(new NBTTagList(), null, null);
    }

    @Override
    public void writeTagContents(DataOutput dataOutput) throws IOException {
        this.tag.writeTagContents(dataOutput);
    }

    @Override
    public void readTagContents(DataInput dataInput) throws IOException {
        this.tag.readTagContents(dataInput);
    }

    public DataEntry getRoot() {
        return root;
    }

    public void save() {
        this.context.save(this.root);
    }

    public StorageContext getContext() {
        return context;
    }

    public StorageTable getStorageTable(int position) {
        return new StorageTable(getCompoundTag(position), this.context, this.root);
    }

    public void setStorageTable(int position, StorageTable table) {
        setTag(position, table.tag);
    }

    public void addStorageTable(StorageTable table) {
        addTag(table.tag);
    }

    public StorageList getStorageList(int position) {
        return new StorageList(getTagList(position), this.context, this.root);
    }

    public void setStorageList(int position, StorageList list) {
        setTag(position, list.tag);
    }

    public void addStorageList(StorageList list) {
        addTag(list.tag);
    }


    @Override
    public void setTag(NBTBase hm) {
        this.tag.setTag(hm);
    }

    @Override
    public List<NBTBase> getTagList() {
        return this.tag.getTagList();
    }

    @Override
    public int size() {
        return this.tag.size();
    }

    @Override
    public void clear() {
        this.tag.clear();
    }

    @Override
    public void addTagList(NBTTagList value) {
        this.tag.addTagList(value);
    }

    @Override
    public void addCompoundTag(NBTTagCompound value) {
        this.tag.addCompoundTag(value);
    }

    @Override
    public void add(Object value) {
        this.tag.add(value);
    }

    @Override
    public void addTag(NBTBase value) {
        this.tag.addTag(value);
    }

    @Override
    public void addByte(byte value) {
        this.tag.addByte(value);
    }

    @Override
    public void addShort(short value) {
        this.tag.addShort(value);
    }

    @Override
    public void addInteger(int value) {
        this.tag.addInteger(value);
    }

    @Override
    public void addLong(long value) {
        this.tag.addLong(value);
    }

    @Override
    public void addFloat(float value) {
        this.tag.addFloat(value);
    }

    @Override
    public void addDouble(double value) {
        this.tag.addDouble(value);
    }

    @Override
    public void addString(String value) {
        this.tag.addString(value);
    }

    @Override
    public void addBoolean(boolean value) {
        this.tag.addBoolean(value);
    }

    @Override
    public void addByteArray(byte[] value) {
        this.tag.addByteArray(value);
    }

    @Override
    public void addIntArray(int[] value) {
        this.tag.addIntArray(value);
    }

    @Override
    public <I extends Enum<I>> void addEnum(I value) {
        this.tag.addEnum(value);
    }

    @Override
    public <I> void addSerializable(I obj, NBTObjectWriter<I> writer) {
        this.tag.addSerializable(obj, writer);
    }

    @Override
    public void setTag(int position, NBTBase tag) {
        this.tag.setTag(position, tag);
    }

    @Override
    public void setTagList(int position, NBTTagList value) {
        this.tag.setTagList(position, value);
    }

    @Override
    public void setCompoundTag(int position, NBTTagCompound value) {
        this.tag.setCompoundTag(position, value);
    }

    @Override
    public void set(int position, Object value) {
        this.tag.set(position, value);
    }

    @Override
    public void setByte(int position, byte value) {
        this.tag.setByte(position, value);
    }

    @Override
    public void setShort(int position, short value) {
        this.tag.setShort(position, value);
    }

    @Override
    public void setInteger(int position, int value) {
        this.tag.setInteger(position, value);
    }

    @Override
    public void setLong(int position, long value) {
        this.tag.setLong(position, value);
    }

    @Override
    public void setFloat(int position, float value) {
        this.tag.setFloat(position, value);
    }

    @Override
    public void setDouble(int position, double value) {
        this.tag.setDouble(position, value);
    }

    @Override
    public void setString(int position, String value) {
        this.tag.setString(position, value);
    }

    @Override
    public void setBoolean(int position, boolean value) {
        this.tag.setBoolean(position, value);
    }

    @Override
    public void setByteArray(int position, byte[] value) {
        this.tag.setByteArray(position, value);
    }

    @Override
    public void setIntArray(int position, int[] value) {
        this.tag.setIntArray(position, value);
    }

    @Override
    public <I extends Enum<I>> void setEnum(int position, I value) {
        this.tag.setEnum(position, value);
    }

    @Override
    public <I> void setSerializable(int position, I obj, NBTObjectWriter<I> writer) {
        this.tag.setSerializable(position, obj, writer);
    }

    @Override
    public NBTBase getTag(int pos) {
        return this.tag.getTag(pos);
    }

    @Override
    public NBTTagList getTagList(int pos) {
        return this.tag.getTagList(pos);
    }

    @Override
    public NBTTagCompound getCompoundTag(int pos) {
        return this.tag.getCompoundTag(pos);
    }

    @Override
    public byte getByte(int position) {
        return this.tag.getByte(position);
    }

    @Override
    public short getShort(int position) {
        return this.tag.getShort(position);
    }

    @Override
    public int getInteger(int position) {
        return this.tag.getInteger(position);
    }

    @Override
    public long getLong(int position) {
        return this.tag.getLong(position);
    }

    @Override
    public float getFloat(int position) {
        return this.tag.getFloat(position);
    }

    @Override
    public double getDouble(int position) {
        return this.tag.getDouble(position);
    }

    @Override
    public String getString(int position) {
        return this.tag.getString(position);
    }

    @Override
    public boolean getBoolean(int position) {
        return this.tag.getBoolean(position);
    }

    @Override
    public byte[] getByteArray(int position) {
        return this.tag.getByteArray(position);
    }

    @Override
    public int[] getIntArray(int position) {
        return this.tag.getIntArray(position);
    }

    @Override
    public <I extends Enum<I>> I getEnum(int position, Class<I> type) {
        return this.tag.getEnum(position, type);
    }

    @Override
    public <I> I getSerializable(int position, NBTObjectReader<I> reader) {
        return this.tag.getSerializable(position, reader);
    }

    @Override
    public String getKey() {
        return this.tag.getKey();
    }

    @Override
    public NBTBase setKey(String string) {
        return this.tag.setKey(string);
    }
}