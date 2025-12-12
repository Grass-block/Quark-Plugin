package org.atcraftmc.starlight.core.data.region;

interface RegionLoadHandler {
    void load(int cx, int cz);

    void unload(int cx, int cz);
}
