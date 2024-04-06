package org.tbstcraft.quark.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.util.FilePath;

import java.io.*;
import java.util.logging.Logger;

public final class Configuration {
    public static final String TEMPLATE_DIR = "/templates/config/%s.yml";
    public static final String FILE_DIR = "%s/config/%s.yml";


    private final Plugin owner;
    private final String ownerId;
    private final YamlConfiguration config = new YamlConfiguration();
    private final String id;

    public Configuration(Plugin owner, String id) {
        this.owner = owner;
        this.ownerId = "quark";
        ConfigDelegation.CONFIG_REGISTRY.put(id, this);
        this.id = id;
        this.load();
        this.sync(false);
    }

    public Configuration(String id) {
        this(Quark.PLUGIN, id);
    }

    public static File getConfigFile(String pluginId, String id) {
        String fileDir = FILE_DIR.formatted(FilePath.pluginFolder(pluginId), id);
        String srcDir = TEMPLATE_DIR.formatted(id);
        return FilePath.tryReleaseAndGetFile(srcDir, fileDir);
    }


    public ConfigurationSection getRootSection(String moduleId) {
        return this.getRootSection().getConfigurationSection(moduleId);
    }

    public ConfigurationSection getRootSection() {
        if (this.config.getConfigurationSection("config") == null) {
            return new YamlConfiguration();
        }
        return this.config.getConfigurationSection("config");
    }

    public void save() {
        try {
            YamlUtil.saveUTF8(this.config, new FileOutputStream(getConfigFile(this.ownerId, this.id)));
        } catch (IOException e) {
            Quark.LOGGER.severe(e.getMessage());
        }
    }

    public void load() {
        try {
            YamlUtil.loadUTF8(this.config, new FileInputStream(getConfigFile(this.ownerId, this.id)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        this.save();
        this.load();
    }

    public void sync(boolean clean) {
        Logger logger = this.owner.getLogger();
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            String srcDir = TEMPLATE_DIR.formatted(this.id);

            InputStream is = this.owner.getClass().getResourceAsStream(srcDir);
            if (is == null) {
                throw new RuntimeException("source not found: " + srcDir);
            }
            YamlUtil.loadUTF8(cfg, is);
            YamlUtil.update(this.config, cfg, clean, 3);
            this.save();
        } catch (Exception e) {
            logger.warning("failed to sync config %s: %s".formatted(this.asString(), e.getMessage()));
        }
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
        String fileDir = FILE_DIR.formatted(FilePath.pluginFolder(this.ownerId), this.id);
        String srcDir = TEMPLATE_DIR.formatted(this.id);
        FilePath.coverFile(srcDir, fileDir);
        this.load();
    }

    @Override
    public String toString() {
        return this.config.saveToString();
    }

    public String asString() {
        return "Configuration{id=%s loader=%s}".formatted(this.id, this.owner.getClass().getName());
    }
}
