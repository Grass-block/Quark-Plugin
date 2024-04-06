package org.tbstcraft.quark.module;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.module.standalone.StandaloneModuleProvider;
import org.tbstcraft.quark.util.api.APIProfile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QuarkModule {
    /**
     * recommended to fill but acceptable if empty
     */
    String version() default "unspecified";

    /**
     * dont care it unless using record.
     */
    String recordFormat() default "_";

    /**
     * not required except using {@link StandaloneModuleProvider}
     */
    String id() default "null";

    boolean beta() default false;

    boolean useLanguage() default true;

    APIProfile[] compatBlackList() default {};

    FeatureAvailability available() default FeatureAvailability.INHERIT;
}
