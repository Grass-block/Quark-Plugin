package org.tbstcraft.quark.util.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * this map is designed for name spaced item placing and indexing.
 * it can simply replace using 2d array by namespace in column,id in row.
 * @param <I>
 */
public class NameSpaceMap <I>{
    private final HashMap<String,I> map=new HashMap<>();
    private final String split;
    private final I fallback;

    /**
     * @param split    split char
     * @param fallback
     */
    public NameSpaceMap(String split, I fallback){
        this.split=split;
        this.fallback = fallback;
    }

    /**
     * namespace item:put
     * if there's no namespace it will create a new one.
     * @param id id
     * @param namespace namespace
     * @param item item
     */
    public void set(String id,String namespace,I item){
        map.put(namespace+this.split+id,item);
    }

    /**
     * use all string as alternative method. for example:
     * "sunrise_studio:jsysb"will do this:
     * namespace=sunrise_studio,id=jsysb
     * @param all string
     * @param item item
     */
    public void set(String all,I item){
        this.set(all.split(this.split)[1],all.split(this.split)[0],item);
    }

    /**
     * get an item using full string.
     * @return nothing
     */
    public I get(String all){
        if(all==null){
            return this.fallback;
        }
        try {
            return this.get(all.split(this.split)[1], all.split(this.split)[0]);
        }catch (ArrayIndexOutOfBoundsException e){
            return this.fallback;
        }
    }

    /**
     * get an item with id and namespace
     * @param id id
     * @param namespace namespace
     * @return item
     * @throws RuntimeException if not found(namespace/id)
     */
    public I get(String id, String namespace) {
        if(!this.map.containsKey(namespace+this.split+id)){
            throw new RuntimeException("item not found:"+namespace+this.split+id);
        }
        return this.map.get(namespace+this.split+id);
    }

    public Collection<String> idList(){
        Collection<String> list=new ArrayList<>();
        CollectionUtil.iterateMap(map, (key, item) -> list.add(key));
        return list;
    }
    public Collection<I> itemList(){
        Collection<I> list=new ArrayList<>();
        CollectionUtil.iterateMap(map, (key, item) -> list.add(item));
        return list;
    }
}
