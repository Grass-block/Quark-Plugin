package org.tbstcraft.quark.util.registry;

import org.tbstcraft.quark.util.container.NameSpaceMap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;


/**
 * pack of {@link NameSpaceMap},used to register.
 *
 * @param <I> item
 * @param <D> item depending on object class
 * @author GrassBlock2022
 */
public class NameSpacedRegisterMap<I, D> extends NameSpaceMap<I> {
    private final NameSpacedRegisterMap<D, ?> depend;

    /**
     * loadGlobalVars a map. If with no depend on then "map" field could be null.
     *
     * @param map depend map.
     */
    public NameSpacedRegisterMap(NameSpacedRegisterMap<D, ?> map, I fallback) {
        super(":", fallback);
        this.depend = map;
    }

    public NameSpacedRegisterMap(NameSpacedRegisterMap<D, ?> map) {
        this(map, null);
    }

    public NameSpacedRegisterMap() {
        this(null, null);
    }

    /**
     * namespace item:put
     * if there's no namespace it will create a new one.
     *
     * @param id        id
     * @param namespace namespace
     * @param item      item
     */
    public void registerItem(String id, String namespace, I item) {
        this.set(id, namespace, item);
    }

    /**
     * use all string as alternative method. for example:
     * "sunrise_studio:jsysb"will do this:
     * namespace=sunrise_studio,id=jsysb
     *
     * @param all  string
     * @param item item
     */
    public void registerItem(String all, I item) {
        registerItem(all.split(":")[1], all.split(":")[0], item);
    }


    /**
     * put a class of {@link I}
     *
     * @param id        id
     * @param namespace namespace
     * @param item      item
     */
    public void registerClass(String id, String namespace, Class<? extends I> item) {
        try {
            this.set(id, namespace, item.getConstructor().newInstance());
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * put a class of {@link I}
     *
     * @param all  full name(jsysb:jsysb)
     * @param item item
     */
    public void registerClass(String all, Class<? extends I> item) {
        registerClass(all.split(":")[1], all.split(":")[0], item);
    }


    /**
     * put a class of getter of{@link I}
     *
     * @param clazz class that contains getter
     * @see ItemGetter,GetterDepend
     */
    public void registerGetter(Class<?> clazz) {
        for (Method m : clazz.getMethods()) {
            ItemGetter getter = m.getAnnotation(ItemGetter.class);
            if (getter != null) {
                GetterDepend dep = m.getAnnotation(GetterDepend.class);
                try {
                    if (dep != null) {
                        D depend = this.depend.get(dep.id(), dep.namespace());
                        this.set(getter.id(), getter.namespace(), (I) m.invoke(clazz.getConstructor().newInstance(), depend));
                    } else {
                        this.set(getter.id(), getter.namespace(), (I) m.invoke(clazz.getConstructor().newInstance()));
                    }
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * register a class,find all method with {@link ItemRegisterFunc},run and register item.
     *
     * @param clazz target class
     * @see ItemRegisterFunc
     */
    public void registerGetFunctionProvider(Class<?> clazz) {
        try {
            for (Method m : clazz.getMethods()) {
                ItemRegisterFunc getter = m.getAnnotation(ItemRegisterFunc.class);
                if (getter != null) {
                    if (m.getParameters().length == 1 && m.getParameters()[0].getType() == NameSpacedRegisterMap.class) {
                        m.invoke(clazz.getConstructor().newInstance(), this);
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * register a FML-like field holder,when registering,it will scan all field,and register them.
     *
     * @param clazz target class
     * @see FieldRegistryHolder
     * @see FieldRegistry
     */
    public void registerFieldHolder(Class<?> clazz) {
        FieldRegistryHolder holder = clazz.getAnnotation(FieldRegistryHolder.class);
        if (holder == null) {
            for (Field f : clazz.getFields()) {
                FieldRegistry registry = f.getAnnotation(FieldRegistry.class);
                if (!Objects.equals(registry.namespace(), FieldRegistry.DEFAULT_NAMESPACE)) {
                    try {
                        this.registerItem(registry.id(), registry.namespace(), (I) f.get(null));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        } else {
            for (Field f : clazz.getFields()) {
                String namespace = holder.namespace();
                FieldRegistry registry = f.getAnnotation(FieldRegistry.class);
                if (!Objects.equals(registry.namespace(), FieldRegistry.DEFAULT_NAMESPACE)) {
                    namespace = registry.namespace();
                }
                try {
                    this.registerItem(registry.id(), namespace, (I) f.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
