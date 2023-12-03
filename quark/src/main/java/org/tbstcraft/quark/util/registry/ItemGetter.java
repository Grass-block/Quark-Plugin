package org.tbstcraft.quark.util.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * holds an item getter function for registry.
 *
 * <p>could be used for a method which signed as no param and object return.</p>
 *
 * <p>when using {@link NameSpacedRegisterMap}.registerGetter,it will scan all method in class,
 * finding match methods(I get())with this annotated,run and get a method.
 * this could be annotated in static or non-static method.</p>
 *
 * @see NameSpacedRegisterMap
 * @see GetterDepend
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ItemGetter {
    String id();
    String namespace();
}
