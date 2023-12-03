package org.tbstcraft.quark.util.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * this annotation holds an item register function.
 *
 * <p>when registering using {@link NameSpacedRegisterMap}.registerFunctionProvider,
 * it will find all method that annotated and having {@link NameSpacedRegisterMap} as the only param,
 * run  reg.</p>
 * <p>you could register your thing inside the method,or do operation to the map whatever you want.</p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ItemRegisterFunc {
}
