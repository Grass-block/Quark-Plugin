package org.tbstcraft.quark.framework.customcontent.block;

import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QuarkBlock {
    String id();

    DropChance chance() default DropChance.DROP;

    Material icon();

    boolean enchantGlow() default false;
}
