package org.tbstcraft.quark.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface QuarkCommand {
    String name();
    boolean op() default false;
}
