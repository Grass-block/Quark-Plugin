package org.atcraftmc.starlight.framework.module;

import me.gb2022.modular.FeatureAvailability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StandalonePackage {
    String value();

    FeatureAvailability avail() default FeatureAvailability.BOTH;
}
