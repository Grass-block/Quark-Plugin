package org.tbstcraft.quark.util.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * simple util for collection
 *
 * @author GrassBlock2022
 */
public class CollectionUtil {
    /**
     * map iterate
     * @param map target
     * @param action action
     * @param <K> map key Template class
     * @param <V> map value Template class
     */
    public static <K,V>void iterateMap(Map<K,V> map, MapIterationAction<K,V> action){
        Set<K> keys=map.keySet();
        for(K key:keys){
            action.action(key,map.get(key));
        }
    }

    /**
     * wrap a single k-v-pair to an map.
     * @param id key
     * @param t value
     * @return wrapped value
     * @param <T> Template class
     */
    public static <T> HashMap<String, T> wrap(String id, T t) {
        HashMap<String,T> map=new HashMap<>();
        map.put(id, t);
        return map;
    }

    public interface MapIterationAction<K,V>{
        void action(K key,V item);
    }
}
