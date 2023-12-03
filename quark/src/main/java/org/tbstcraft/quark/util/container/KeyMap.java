package org.tbstcraft.quark.util.container;

import java.util.HashMap;

/**
 * a key map for reg.
 * @param <K> key class
 * @param <V> value class(should be key getter)
 *
 * @author GrassBlock2022
 */
public class KeyMap<K extends Key,V extends KeyGetter<K>> {
    public final HashMap<Integer,V> map=new HashMap<>();

    /**
     * add an object,using key as map key.
     * @param v obj
     */
    public V add(V v){
        if (!map.containsKey(v.getKey().hashCode())){
            map.put(v.getKey().hashCode(),v);
        }
        return v;
    }

    /**
     * gather value from map using key.
     * @param k key object.
     * @return value(null if no item).
     */
    public V get(K k){
        return this.map.getOrDefault(k.hashCode(),null);
    }

    /**
     * get if contains value
     *
     * @param k key object.
     * @return contains value.
     */
    public boolean contains(K k) {
        return map.containsKey(k.hashCode());
    }

    /**
     * get map size.
     * @return size
     */
    public int size() {
        return this.map.size();
    }

    /**
     * force add object
     * @param v object
     */
    public void forceAdd(V v){
        map.put(v.getKey().hashCode(),v);
    }

    /**
     * clear container.
     */
    public void clear() {
        this.map.clear();
    }
}
