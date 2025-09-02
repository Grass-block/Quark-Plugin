package org.atcraftmc.starlight.framework.module;

import org.atcraftmc.starlight.framework.FeatureAvailability;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.module.standalone.StandaloneModuleProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SLModule {
    /**
     * recommended to fill but acceptable if empty
     */
    String version() default "1.0";

    /**
     * not required except using {@link StandaloneModuleProvider}
     */
    String id() default "null";

    boolean beta() default false;

    boolean useLanguage() default true;

    APIProfile[] compatBlackList() default {};

    FeatureAvailability available() default FeatureAvailability.INHERIT;

    boolean internal() default false;

    boolean defaultEnable() default true;

    String description() default "No description provided.";


    /**
     * do not care it unless using record.<br>
     * deprecated, please use @Inject("Name;Format(,)") instead.
     */
    @Deprecated
    String[] recordFormat() default {};
}
