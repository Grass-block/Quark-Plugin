package org.tbstcraft.quark.framework.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//todo: command permission test
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QuarkCommand {
    String NO_INFO = "(no information)";

    String name();

    boolean op() default false;

    boolean playerOnly() default false;

    String permission() default NO_INFO;

    String usage() default NO_INFO;

    String description() default NO_INFO;

    String[] aliases() default {};

    boolean eventBased() default false;

    Class<? extends AbstractCommand>[] subCommands() default {};
}
