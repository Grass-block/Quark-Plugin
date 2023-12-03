package org.tbstcraft.quark.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.util.FilePath;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ConfigFile {
    public static final HashMap<String, ConfigFile> REGISTERED_CACHE = new HashMap<>();

    private final YamlConfiguration config = new YamlConfiguration();
    private final String packageId;

    public ConfigFile(String packageId) {
        REGISTERED_CACHE.put(packageId, this);
        this.packageId = packageId;
        this.load();
    }

    public static void reloadAll() {
        for (ConfigFile file : REGISTERED_CACHE.values()) {
            file.load();
        }
    }

    public static void restoreAll() {
        for (ConfigFile file : REGISTERED_CACHE.values()) {
            file.restore();
        }
    }

    public void load() {
        try {
            this.config.load(FilePath.configFile(this.packageId));
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigurationSection getRootSection(String moduleId) {
        return this.getRootSection().getConfigurationSection(moduleId);
    }

    public ConfigurationSection getRootSection() {
        return Objects.requireNonNull(this.config.getConfigurationSection("config"));
    }

    public void save() {
        try {
            this.config.save(FilePath.configFile(this.packageId));
        } catch (IOException e) {
            SharedObjects.LOGGER.severe(e.getMessage());
        }
    }

    public void reload() {
        this.save();
        this.load();
    }

    public boolean isEnabled(String moduleId, String id) {
        return this.getRootSection(moduleId).getBoolean(id);
    }

    public void set(String moduleId, String path, Object arg) {
        this.getRootSection(moduleId).set(path, arg);
    }

    public ConfigurationSection getConfig(String moduleId) {
        return this.getRootSection(moduleId);
    }

    public ConfigurationSection getConfig() {
        return this.getRootSection();
    }

    public void restore() {
        FilePath.coverConfigFile(this.packageId);
        this.load();
    }
}
