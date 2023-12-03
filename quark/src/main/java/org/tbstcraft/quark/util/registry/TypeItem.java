package org.tbstcraft.quark.util.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * type on a top of class,use to constructing registry.
 * so you don`t need to type id when registering
 *
 * value:name of structure
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeItem {
    String value();
}