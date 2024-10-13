package org.tbstcraft.quark.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.Queries;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.util.ExceptionUtil;
import org.tbstcraft.quark.util.FilePath;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class ConfigurationPack {
    public static final Pattern REPLACE = Pattern.compile("\\$\\{[a-z-_]+}");
    public static final String FILE_EDIT_HEADER = """
            # This is real configuration/message file where you can do your modifications
            # Add ONLY items you modified here.full files are not  required.
            # This file will not be updated, remind item rename once you updated.
            #
            # For reference template file, go %s
            """;
    public static final String FILE_TEMPLATE_HEADER = """
            #this file is only for template which shows you all the items and defaults.
            #structures are as follows here, you can choose only items that you want to modify.
            #DO NOT EDIT THIS FILE. IT IS JUST A TEMPLATE AND WILL BEE COVERED EVERY START.
            #
            #do your modifications in file: %s
            """;

    protected final String id;
    protected final Plugin provider;
    protected final YamlConfiguration dom = new YamlConfiguration();

    protected ConfigurationPack(String id, Plugin provider) {
        this.id = id;
        this.provider = provider;
    }

    @Override
    public abstract String toString();

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String getId() {
        return id;
    }

    public Plugin getProvider() {
        return provider;
    }

    //----[file]----
    public String getTemplateDirectory() {
        return getTemplateFile();
    }

    public String getStorageDirectory() {
        return getStorageFile();
    }


    public abstract String getTemplateFile();

    public abstract String getStorageFile();

    public abstract String getTemplateResource();


    public File createStorageFile(boolean enforce) {
        var file = new File(FilePath.pluginFolder("quark") + getStorageFile());

        if (file.exists() && file.length() > 0) {
            if (!enforce) {
                return file;
            }
        }

        if (file.getParentFile().mkdirs()) {
            Quark.LOGGER.info("created storage file folder: " + file.getParentFile().getAbsolutePath());
        }
        try {
            if (file.createNewFile()) {
                Quark.LOGGER.info("created storage file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Quark.LOGGER.info("failed to create storage file: " + file.getAbsolutePath());
            ExceptionUtil.log(e);
        }

        try (var stream = new FileOutputStream(file, false); var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write(FILE_EDIT_HEADER.formatted(getTemplateFile()));
            writer.write(getRootSectionName() + ":");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Quark.LOGGER.info("failed to write storage file: " + file.getAbsolutePath());
            ExceptionUtil.log(e);
        }

        return file;
    }

    public void createTemplateFile() {
        var file = new File(FilePath.pluginFolder("quark") + getTemplateFile());

        if (file.getParentFile().mkdirs()) {
            Quark.LOGGER.info("created template file folder: " + file.getParentFile().getAbsolutePath());
        }
        try {
            if (file.createNewFile()) {
                Quark.LOGGER.info("created template file: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Quark.LOGGER.info("failed to create template file: " + file.getAbsolutePath());
            ExceptionUtil.log(e);
        }

        try (var stream = new FileOutputStream(file, false); var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write(FILE_TEMPLATE_HEADER.formatted(getStorageFile()));
            try (var template = provider.getClass().getResourceAsStream(getTemplateResource())) {
                assert template != null;
                writer.write(new String(template.readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Quark.LOGGER.info("failed to write storage file: " + file.getAbsolutePath());
            ExceptionUtil.log(e);
        }
    }

    public File getFile() {
        return FilePath.tryReleaseAndGetFile(getTemplateDirectory(), getStorageDirectory());
    }

    private void merge(ConfigurationSection current, ConfigurationSection mod) {
        if (mod == null) {
            return;
        }
        for (String key : current.getKeys(false)) {
            if (current.isConfigurationSection(key)) {
                merge(Objects.requireNonNull(current.getConfigurationSection(key)), mod.getConfigurationSection(key));
                continue;
            }

            current.set(key, mod.get(key));
        }
    }

    public String processDOM(String s) {
        return Queries.applyEnvironmentVars(s);
    }

    public void load() {
        var custom = new YamlConfiguration();

        try (var template = provider.getClass()
                .getResourceAsStream(getTemplateResource()); var mod = new FileInputStream(createStorageFile(false))) {
            assert template != null;

            this.dom.loadFromString(processDOM(new String(template.readAllBytes(), StandardCharsets.UTF_8)).replace("\ud83c\udf10", ""));
            custom.loadFromString(processDOM(new String(mod.readAllBytes(), StandardCharsets.UTF_8)));

            merge(this.dom, custom);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        /*
        try {
            File f = new File(this.getStorageDirectory());
            if (!f.exists() || f.length() == 0) {
                this.restore();
            }
            this.dom.load(this.getStorageDirectory());
            sync(false);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        */
    }

    public void save() {
        /*
        try {
            YamlUtil.saveUTF8(this.dom, new FileOutputStream(getFile()));
        } catch (IOException e) {
            this.provider.getLogger().severe(e.getMessage());
        }

         */
    }

    public void reload() {
        this.save();
        this.load();
    }

    public void restore() {
        this.createStorageFile(true);
        load();
    }

    public void sync(boolean clean) {
        Logger logger = this.provider.getLogger();
        try {
            var template = new YamlConfiguration();
            var srcDir = getTemplateDirectory();

            var is = this.provider.getClass().getResourceAsStream(srcDir);
            if (is == null) {
                throw new RuntimeException("source not found: " + srcDir);
            }
            YamlUtil.loadUTF8(template, is);
            YamlUtil.update(this.dom, template, clean, 3);
            this.save();
        } catch (Exception e) {
            logger.warning("failed to sync config %s: %s".formatted(this.toString(), e.getMessage()));
        }
    }


    //----[dom]----
    public abstract String getRootSectionName();

    public String getDom() {
        return this.dom.saveToString();
    }

    public ConfigurationSection getRootSection() {
        return this.dom.getConfigurationSection(getRootSectionName());
    }

    public ConfigurationSection getNamespace(String namespace) {
        return this.getRootSection().getConfigurationSection(namespace);
    }

    public boolean hasEntry(String namespace, String id) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (section == null) {
            return false;
        }
        return section.contains(id);
    }

    public Set<String> getEntries(String namespace) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (section == null) {
            return Set.of();
        }
        return section.getKeys(false);
    }

    public Set<String> getNamespaces() {
        if (this.getRootSection() == null) {
            return Set.of();
        }
        return this.getRootSection().getKeys(false);
    }

    public String getRegisterId() {
        return getId();
    }
}
