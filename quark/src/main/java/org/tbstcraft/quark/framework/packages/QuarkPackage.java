package org.tbstcraft.quark.framework.packages;

import org.tbstcraft.quark.FeatureAvailability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface QuarkPackage {
    String value() default "";

    FeatureAvailability available() default FeatureAvailability.PREMIUM;

    //
    String description() default "";

    String version() default "";

    String author() default "";
}
