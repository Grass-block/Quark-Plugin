package org.tbstcraft.quark.util.registry;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * holds a FML-like static field for registering.
 *
 * <p>when there is a {@link FieldRegistryHolder} outside,you no longer need to type namespace here.
 * you could still write namespace,and it will cover the namespace from {@link FieldRegistryHolder} outside</p>
 *
 * <p>but if there is no annotating on class, you NEED to type namespace,otherwise you could not complete a registry.</p>
 *
 * @author GrassBlock2022
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldRegistry {
    String DEFAULT_NAMESPACE = "__DEFAULT__";

    String id();
    String namespace() default DEFAULT_NAMESPACE;
}
