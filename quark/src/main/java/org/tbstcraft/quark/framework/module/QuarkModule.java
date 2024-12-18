package org.tbstcraft.quark.framework.module;

import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.framework.module.standalone.StandaloneModuleProvider;

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

    String description() default "no info";


    /**
     * do not care it unless using record.<br>
     * deprecated, please use @Inject("Name;Format(,)") instead.
     */
    @Deprecated
    String[] recordFormat() default {};
}
