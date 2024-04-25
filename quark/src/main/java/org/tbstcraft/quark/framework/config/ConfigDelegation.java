package org.tbstcraft.quark.framework.config;

import java.util.HashMap;

public interface ConfigDelegation {
    HashMap<String, Configuration> CONFIG_REGISTRY = new HashMap<>();
    HashMap<String, Language> LANGUAGE_REGISTRY = new HashMap<>();

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

    static void reloadLanguages() {
        for (Language languageFile : LANGUAGE_REGISTRY.values()) {
            languageFile.reload();
        }
    }

    static void restoreLanguages() {
        for (Language languageFile : LANGUAGE_REGISTRY.values()) {
            languageFile.restore();
        }
    }

    static void syncLanguages(boolean clean) {
        for (Language lang : LANGUAGE_REGISTRY.values()) {
            lang.sync(clean);
        }
    }

    static Language getLanguage(String id) {
        return LANGUAGE_REGISTRY.get(id);
    }

    static Configuration getConfig(String arg) {
        return CONFIG_REGISTRY.get(arg);
    }
}
