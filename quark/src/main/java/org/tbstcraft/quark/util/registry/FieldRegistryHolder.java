package org.tbstcraft.quark.util.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * holds a FML like register holder in class.(FML-ObjectHolder)
 * <p>provides a namespace,so you don`t need to type namespace again and again when you are registering
 * a bunch of items</p>
 * <p>this annotation should be placed on class, witch provides namespace to all items in class.</p>
 * @author GrassBlock2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldRegistryHolder {
    String namespace();
}
