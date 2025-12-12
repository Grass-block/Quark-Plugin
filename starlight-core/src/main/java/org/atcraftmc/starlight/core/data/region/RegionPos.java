package org.atcraftmc.starlight.core.data.region;

import org.bukkit.Location;

public interface RegionPos {
    int MAX_PROVIDER_CAPACITY = 16384;
    int REGION_ASPECT = 128;
    int LOAD_RADIUS = 1;
    int LIFETIME_INCREMENT = 400;

    static long getRegionKey(Location location) {
        var rx = location.getBlockX() / REGION_ASPECT;
        var rz = location.getBlockZ() / REGION_ASPECT;

        return encode(rx, rz);
    }

    static long encode(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

    static int getX(long l) {
        return (int) (l & 0xFFFFFFFFL);
    }

    static int getZ(long l) {
        return (int) (l >>> 32 & 0xFFFFFFFFL);
    }
}
