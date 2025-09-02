package org.atcraftmc.starlight.framework.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SLService {
    Class<? extends Service> impl() default Service.class;

    String id();

    String[] requiredBy() default {};

    ServiceLayer layer() default ServiceLayer.USER;
}
