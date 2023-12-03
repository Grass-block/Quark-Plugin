package org.tbstcraft.quark.util.container;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * allows you to access map from both side,can also use as normal hashmap
 * @param <K>key
 * @param <V>value
 *
 * @author GrassBlock2022
 */
public class MultiMap<K,V> implements Map<K,V> {
    private final HashMap<K,V> kvHashMap=new HashMap<>();
    private final HashMap<V,K> vkHashMap=new HashMap<>();

    /**
     * {@inheritDoc}
     * @param k key with which the specified value is to be associated
     * @param v value to be associated with the specified key
     */
    public V put(K k, V v){
        this.kvHashMap.put(k,v);
        this.vkHashMap.put(v,k);
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.kvHashMap.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.kvHashMap.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return this.kvHashMap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return this.kvHashMap.containsKey(value);
    }

    /**
     * {@inheritDoc}
     */
    public V get(Object obj){
        return this.kvHashMap.get(obj);
    }

    /**
     * return the specific key of given value.
     * @param v value
     * @return key
     */
    public K of(V v){
        return this.vkHashMap.get(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(Object k){
        this.vkHashMap.remove(this.kvHashMap.get(k));
        return this.kvHashMap.remove(k);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        CollectionUtil.iterateMap(m, this::put);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.kvHashMap.clear();
        this.vkHashMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet() {
        return this.kvHashMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        return this.kvHashMap.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.kvHashMap.entrySet();
    }
}
