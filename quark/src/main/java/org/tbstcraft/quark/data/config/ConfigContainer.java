package org.tbstcraft.quark.data.config;

import java.util.HashMap;

public interface ConfigContainer {
    HashMap<String, Configuration> CONFIG_REGISTRY = new HashMap<>();

    static void reloadConfigs() {
        for (Configuration file : CONFIG_REGISTRY.values()) {
            file.load();
        }
    }

    static void restoreConfigs() {
        for (Configuration file : CONFIG_REGISTRY.values()) {
            file.restore();
        }
    }

    static void syncConfigs(boolean clean) {
        for (Configuration file : CONFIG_REGISTRY.values()) {
            file.sync(clean);
        }
    }

    static Configuration getConfig(String arg) {
        return CONFIG_REGISTRY.get(arg);
    }
}
