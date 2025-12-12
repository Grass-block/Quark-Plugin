package org.atcraftmc.starlight.framework;

import org.atcraftmc.starlight.util.ProductMetadata;

import java.io.File;

public interface SLPluginConcept {
    ClassLoader classLoader();

    File getFile();

    ProductMetadata getMetadata();
}
