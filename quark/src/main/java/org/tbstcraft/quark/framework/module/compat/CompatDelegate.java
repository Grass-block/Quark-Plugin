package org.tbstcraft.quark.framework.module.compat;

import org.tbstcraft.quark.util.platform.APIProfile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompatDelegate {
    APIProfile[] value();
}
