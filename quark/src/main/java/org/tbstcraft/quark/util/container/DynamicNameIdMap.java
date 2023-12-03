package org.tbstcraft.quark.util.container;

import org.tbstcraft.quark.util.nbt.NBTTagCompound;

/**
 * allocate a space-friendly short alias name(short) to string id.
 * <p>works like FMLNameSpacedRegistry,but a bit different is it support frag manage.</p>
 *
 * @author GrassBlock2022
 */
public class DynamicNameIdMap {
    public short[] array;
    public final MultiMap<String,Short> mapping=new MultiMap<>();
    public short counter;

    public DynamicNameIdMap(int size) {
        this.array = new short[size];
    }

    /**
     * insert a value to array.
     * @param index index (pos).
     * @param id target value.
     */
    public void set(int index,String id){
        if(!this.mapping.containsKey(id)){
             this.alloc(id);
        }
        this.array[index]=this.mapping.get(id);
    }

    /**
     * alloc a new num id for string
     * @param id id that needs to allocate.
     */
    public void alloc(String id){
        this.mapping.put(id,this.counter);
        this.counter++;
    }

    /**
     * manage fragment of mapping,to lower used space,resort mapping.
     *
     * <p>cause a bit time when its big,so not recommended to use really often.</p>
     */
    public void manageFragment(){
        String[] raw=new String[this.array.length];
        for (int i=0;i<this.array.length;i++){
            raw[i]=this.mapping.of(this.array[i]);
        }
        this.mapping.clear();
        this.counter=0;
        for (int i=0;i<this.array.length;i++){
            this.set(i,raw[i]);
        }
    }

    public String[] getArray(){
        String[] raw=new String[this.array.length];
        for (int i=0;i<this.array.length;i++){
            raw[i]=this.mapping.of(this.array[i]);
        }
        return raw;
    }

    /**
     * get value in given index.
     * @param index index
     * @return value
     */
    public String get(int index){
        return this.mapping.of(this.array[index]);
    }

    public void fill(String id) {
        for (int i=0;i<this.array.length;i++){
            this.set(i,id);
        }
    }

    /**
     * export to nbt tag,encode all data(a complex data storage).
     * @return tag
     */
    public NBTTagCompound export() {
        NBTTagCompound tag=new NBTTagCompound();
        NBTTagCompound map=new NBTTagCompound();
        CollectionUtil.iterateMap(this.mapping, (key, item) -> map.setInteger(key,item));
        tag.setCompoundTag("map",map);
        tag.setIntArray("data",ArrayUtil.short2int(this.array));
        return tag;
    }

    /**
     * import from nbt tag,cover all exist data.
     * @param tag tag
     */
    public void setData(NBTTagCompound tag){
        this.mapping.clear();
        NBTTagCompound map=tag.getCompoundTag("map");
        CollectionUtil.iterateMap(map.getTagMap(), (key, item) -> mapping.put(key, (short) map.getInteger(key)));
        this.array=ArrayUtil.int2short(tag.getIntArray("data"));
        this.manageFragment();
    }

    /**
     * fill data in,cover origin data.
     * @param raw data
     */
    public void setArr(String[] raw) {
        for (int i=0;i<this.array.length;i++){
            this.set(i,raw[i]);
        }
    }
}
