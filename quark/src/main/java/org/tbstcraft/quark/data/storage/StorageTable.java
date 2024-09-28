package org.tbstcraft.quark.data.storage;

import me.gb2022.commons.nbt.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

public class StorageTable extends NBTTagCompound {
    final NBTTagCompound tag;
    private final StorageContext context;
    final DataEntry root;

    public StorageTable(NBTTagCompound tag, StorageContext context, DataEntry root) {
        this.tag = tag;
        this.context = context;
        this.root = root;
    }

    public StorageTable() {
        this(new NBTTagCompound(), null, null);
    }

    public void save() {
        this.context.save(this.root);
    }

    public StorageContext getContext() {
        return context;
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

    public StorageTable getTable(String id) {
        return new StorageTable(getCompoundTag(id), this.context, this.root);
    }

    public void setTable(String id, StorageTable table) {
        setTag(id, table.tag);
    }

    public StorageList getList(String id) {
        return new StorageList(getTagList(id), this.context, this.root);
    }

    public void setList(String id, StorageList list) {
        setTag(id, list.tag);
    }

    @Override
    public Map<String, NBTBase> getTagMap() {
        return this.tag.getTagMap();
    }

    @Override
    public boolean hasKey(String string) {
        return this.tag.hasKey(string);
    }

    @Override
    public void remove(String target) {
        this.tag.remove(target);
    }

    @Override
    public void clear() {
        this.tag.clear();
    }

    @Override
    public void setTag(String string, NBTBase value) {
        this.tag.setTag(string, value);
    }

    @Override
    public void setCompoundTag(String string, NBTTagCompound value) {
        this.tag.setCompoundTag(string, value);
    }

    @Override
    public void set(String id, Object value) {
        this.tag.set(id, value);
    }

    @Override
    public void setByte(String string, byte value) {
        this.tag.setByte(string, value);
    }

    @Override
    public void setIntArray(String string, int[] value) {
        this.tag.setIntArray(string, value);
    }

    @Override
    public void setShort(String string, short value) {
        this.tag.setShort(string, value);
    }

    @Override
    public void setInteger(String string, int value) {
        this.tag.setInteger(string, value);
    }

    @Override
    public void setLong(String string, long value) {
        this.tag.setLong(string, value);
    }

    @Override
    public void setFloat(String string, float value) {
        this.tag.setFloat(string, value);
    }

    @Override
    public void setDouble(String path, double value) {
        this.tag.setDouble(path, value);
    }

    @Override
    public void setBoolean(String string, boolean value) {
        this.tag.setBoolean(string, value);
    }

    @Override
    public void setString(String string1, String value) {
        this.tag.setString(string1, value);
    }

    @Override
    public void setByteArray(String string, byte[] value) {
        this.tag.setByteArray(string, value);
    }

    @Override
    public <T extends Enum<T>> void setEnum(String id, T value) {
        this.tag.setEnum(id, value);
    }

    @Override
    public <T> void setSerializable(String id, T obj, NBTObjectWriter<T> writer) {
        this.tag.setSerializable(id, obj, writer);
    }

    @Override
    public NBTBase getTag(String id) {
        return this.tag.getTag(id);
    }

    @Override
    public NBTTagCompound getCompoundTag(String string) {
        return this.tag.getCompoundTag(string);
    }

    @Override
    public NBTTagList getTagList(String string) {
        return this.tag.getTagList(string);
    }

    @Override
    public byte getByte(String id) {
        return this.tag.getByte(id);
    }

    @Override
    public short getShort(String id) {
        return this.tag.getShort(id);
    }

    @Override
    public int getInteger(String id) {
        return this.tag.getInteger(id);
    }

    @Override
    public long getLong(String id) {
        return this.tag.getLong(id);
    }

    @Override
    public float getFloat(String id) {
        return this.tag.getFloat(id);
    }

    @Override
    public double getDouble(String path) {
        return this.tag.getDouble(path);
    }

    @Override
    public String getString(String id) {
        return this.tag.getString(id);
    }

    @Override
    public byte[] getByteArray(String id) {
        return this.tag.getByteArray(id);
    }

    @Override
    public int[] getIntArray(String id) {
        return this.tag.getIntArray(id);
    }

    @Override
    public boolean getBoolean(String id) {
        return this.tag.getBoolean(id);
    }

    @Override
    public <T extends Enum<T>> T getEnum(String id, Class<T> type) {
        return this.tag.getEnum(id, type);
    }

    @Override
    public <T> T getSerializable(String id, NBTObjectReader<T> reader) {
        return this.tag.getSerializable(id, reader);
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
