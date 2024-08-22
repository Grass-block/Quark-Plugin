package org.tbstcraft.quark.deprecated.command_driver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandTabCompleter {
    String path();

    String permission() default "+quark.command.default";
}
