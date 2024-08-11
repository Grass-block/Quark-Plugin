package org.tbstcraft.quark.data.language;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class LanguagePack {
    public static final String TEMPLATE_DIR = "/templates/lang/%s.%s.yml";
    public static final String FILE_DIR = "%s/lang/%s/%s.yml";
    private final String id;
    private final String locale;

    protected final YamlConfiguration dom = new YamlConfiguration();

    private final Plugin owner;

    public LanguagePack(String id, String locale, Plugin owner) {
        this.id = id;
        this.locale = locale;
        this.owner = owner;
    }

    static boolean existType(Plugin owner, String id, String locale) {
        File f = new File(FILE_DIR.formatted(
                FilePath.pluginFolder(Quark.PLUGIN_ID),
                Language.locale(locale),
                id
        ));
        if (f.exists() && f.length() > 0) {
            return true;
        }
        String srcDir = TEMPLATE_DIR.formatted(id, locale);
        InputStream is = owner.getClass().getResourceAsStream(srcDir);

        if (is != null) {
            try {
                is.close();
            } catch (IOException ignored) {
            }
            return true;
        }
        return false;
    }

    static String error(String state, String namespace, String id) {
        return "ERROR: %s(%s:%s)".formatted(state, namespace, id);
    }

    public ConfigurationSection getRootSection() {
        return this.dom.getConfigurationSection("language");
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

    public void sync(boolean clean) {
        YamlConfiguration template = new YamlConfiguration();
        String srcDir = TEMPLATE_DIR.formatted(this.id, locale);
        InputStream is = this.owner.getClass().getResourceAsStream(srcDir);
        if (is == null) {
            return;
        }
        YamlUtil.loadUTF8(template, is);
        YamlUtil.update(this.dom, template, clean, 3);

        this.save();
    }

    public void load() {
        try {
            File f = new File(fileDir());
            if (!f.exists() || f.length() == 0) {
                this.restore();
            }
            this.dom.load(fileDir());
            sync(false);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void restore() {
        String srcDir = TEMPLATE_DIR.formatted(this.id, locale);
        InputStream is = this.owner.getClass().getResourceAsStream(srcDir);
        if (is == null) {
            return;
        }
        YamlUtil.loadUTF8(this.dom, is);

        this.save();
    }

    private void save() {
        try {
            this.dom.save(fileDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fileDir() {
        return FILE_DIR.formatted(
                FilePath.pluginFolder(Quark.PLUGIN.getName()),
                this.locale,
                this.id
        );
    }

    public Set<String> getEntries(String namespace) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (section == null) {
            return Set.of();
        }
        return section.getKeys(false);
    }

    public Set<String> getNamespaces() {
        return this.getRootSection().getKeys(false);
    }

    public Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (String entry : getNamespaces()) {
            for (String k : getEntries(entry)) {
                keys.add("%s:%s".formatted(entry, k));
            }
        }

        return keys;
    }

    public String getId() {
        return this.id;
    }

    public String getLocale() {
        return locale;
    }

    public Object getObject(String full) {
        return getObject(full.split(":")[0], full.split(":")[1]);
    }

    public Object getObject(String namespace, String id) {
        ConfigurationSection section = this.getNamespace(namespace);
        return section.get(id);
    }


    @Override
    public String toString() {
        return this.id + ":" + this.locale;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
