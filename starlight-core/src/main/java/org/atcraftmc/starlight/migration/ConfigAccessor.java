package org.atcraftmc.starlight.migration;

import org.atcraftmc.qlib.config.ConfigEntry;

import java.util.List;

public interface ConfigAccessor {
    static int getInt(ConfigEntry config, String id) {
        return config.value(id).intValue();
    }

    static boolean getBool(ConfigEntry config, String broadcast) {
        return config.value(broadcast).bool();
    }

    static <I> List<I> configList(ConfigEntry config, String s, Class<I> stringClass) {
        return config.value(s).list(stringClass);
    }

    static float getFloat(ConfigEntry config, String s) {
        return config.value(s).floatValue();
    }
}
