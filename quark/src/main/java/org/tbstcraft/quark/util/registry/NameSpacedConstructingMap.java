package org.tbstcraft.quark.util.registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * hols an item constructing registration
 * @param <I> Template class
 */
public class NameSpacedConstructingMap<I> {
    private final NameSpacedRegisterMap<Class<? extends I>,?> map=new NameSpacedRegisterMap<>(null);
    private final Class<?>[] params;

    /**
     * simple initializer
     * @param params object loadGlobalVars args
     */
    public NameSpacedConstructingMap(Class<?>... params) {
        this.params = params;
    }

    /**
     * namespace item:put
     * if there's no namespace it will create a new one.
     *
     * @param id        id
     * @param namespace namespace
     * @param item      item
     */
    public void registerItem(String id, String namespace, Class<? extends I> item) {
        map.registerItem(id,namespace,item);
    }

    /**
     * use all string as alternative method. for example:
     * "sunrise_studio:jsysb"will do this:
     * namespace=sunrise_studio,id=jsysb
     *
     * @param all  string
     * @param item item
     */
    public void registerItem(String all, Class<? extends I> item) {
        registerItem(all.split(":")[1], all.split(":")[0], item);
    }

    /**
     * create a new instance from id.
     * @param all id
     * @param initArgs loadGlobalVars args
     * @return object
     */
    public I create(String all,Object... initArgs){
        try {
            return map.get(all).getDeclaredConstructor(params).newInstance(initArgs);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * create a new instance from id.
     * @param namespace,id id
     * @param initArgs loadGlobalVars args
     * @return object
     */
    public I create(String id,String namespace,Object... initArgs){
        try {
            return map.get(id, namespace).getDeclaredConstructor(params).newInstance(initArgs);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * create all instances in mapping.
     * @param initArgs arg
     * @return created object
     */
    public Map<String,I> createAll(Object... initArgs){
        Map<String,I> map=new HashMap<>();
        for (String all:this.map.idList()){
            map.put(all,create(all,initArgs));
        }
        return map;
    }

    /**
     * register item class,using {@link TypeItem} as id getter.
     * @param item class
     * @throws RuntimeException if item does not contain "typeitem" annotation.
     */
    public void registerItem(Class<? extends I> item) {
        TypeItem a=item.getDeclaredAnnotation(TypeItem.class);
        if(a==null){
            throw new RuntimeException("item does not contains TypeItem annotation,so can`t auto reg.");
        }
        registerItem(a.value(),item);
    }

    /**
     * register a class,find all method with {@link ItemRegisterFunc},run and register item.
     * @param clazz target class
     *
     * @see ItemRegisterFunc
     */
    public void registerGetFunctionProvider(Class<?> clazz) {
        try {
            for (Method m : clazz.getMethods()) {
                ItemRegisterFunc getter = m.getAnnotation(ItemRegisterFunc.class);
                if (getter != null) {
                    if (m.getParameters().length == 1 && m.getParameters()[0].getType() == NameSpacedConstructingMap.class) {
                        m.invoke(clazz.getConstructor().newInstance(), this);
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public NameSpacedRegisterMap<Class<? extends I>, ?> getMap() {
        return map;
    }
}
