package org.tbstcraft.quark.framework.packages;

import org.tbstcraft.quark.FeatureAvailability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface QuarkPackage {
    String value() default "";

    // TODO: 2024/3/12 FULL package activation check
    FeatureAvailability available() default FeatureAvailability.PREMIUM;

    boolean config() default true;

    boolean lang() default true;
}
