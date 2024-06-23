package org.tbstcraft.quark.data.language;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.data.config.YamlUtil;
import org.tbstcraft.quark.util.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LanguagePack {
    public static final String TEMPLATE_DIR = "/templates/lang/%s.%s.yml";
    public static final String FILE_DIR = "%s/lang/%s/%s.yml";

    private final String id;
    private final Locale locale;
    private final YamlConfiguration config = new YamlConfiguration();

    private final Plugin owner;

    public LanguagePack(String id, Locale locale, Plugin owner) {
        this.id = id;
        this.locale = locale;
        this.owner = owner;
    }

    static boolean existType(Plugin owner, String id, Locale locale) {
        File f = new File(FILE_DIR.formatted(
                FilePath.pluginFolder(owner.getName()),
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
        return this.config.getConfigurationSection("language");
    }

    public ConfigurationSection getNamespace(String namespace) {
        return this.getRootSection().getConfigurationSection(namespace);
    }

    public String getMessage(String namespace, String id) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (section == null) {
            return error("NS_NOT_FOUND", namespace, id);
        }

        if (!section.contains(id)) {
            return error("MSG_NOT_FOUND", namespace, id);
        }

        if (!section.isString(id)) {
            List<String> list = section.getStringList(id);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String s2 : list) {
                i++;
                sb.append(s2);
                if (i < list.size() - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }
        return section.getString(id);
    }

    public List<String> getMessageList(String namespace, String id) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (section == null) {
            return Collections.singletonList(error("NS_NOT_FOUND", namespace, id));
        }

        if (!section.contains(id)) {
            return Collections.singletonList(error("MSG_NOT_FOUND", namespace, id));
        }

        if (!section.isList(id)) {
            return Collections.singletonList(error("NOT_LIST", namespace, id));
        }
        return section.getStringList(id);
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

        YamlUtil.update(this.config, template, clean, 3);

        this.save();
    }

    public void load() {
        try {
            File f = new File(fileDir());
            if (!f.exists() || f.length() == 0) {
                this.restore();
            }
            this.config.load(fileDir());
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
        YamlUtil.loadUTF8(this.config, is);
        this.save();
    }

    private void save() {
        try {
            this.config.save(fileDir());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fileDir() {
        return FILE_DIR.formatted(
                FilePath.pluginFolder(this.owner.getName()),
                Language.locale(this.locale),
                this.id
        );
    }

    public Set<String> getEntries(String namespace) {
        ConfigurationSection section = this.getNamespace(namespace);
        if (namespace == null) {
            return Set.of();
        }
        return section.getKeys(false);
    }

    public Set<String> getNamespaces() {
        return this.getRootSection().getKeys(false);
    }
}
