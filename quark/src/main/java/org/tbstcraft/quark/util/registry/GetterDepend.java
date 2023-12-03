package org.tbstcraft.quark.util.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * holds an item getter function dependency for registry in{@link NameSpacedRegisterMap},used when using {@link ItemGetter}
 *
 * <p>could be used for a method which signed as no param and object return.</p>
 *
 * <p>when using {@link ItemGetter}.register,when a method is attached with this annotation,
 * register will find a dependent item in map.run method as a first parameter.</p>
 *
 * <p>within this annotation,target method should have one param.</p>
 *
 * @see NameSpacedRegisterMap
 * @see ItemGetter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetterDepend {
    String id();
    String namespace();
}
