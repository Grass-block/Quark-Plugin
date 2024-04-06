package org.tbstcraft.quark.module.compat;

import org.tbstcraft.quark.util.api.APIProfile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompatDelegate {
    APIProfile[] value();
}
