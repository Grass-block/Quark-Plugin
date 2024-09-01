package org.tbstcraft.quark.data.language;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.ConfigurationPack;
import org.tbstcraft.quark.util.FilePath;

import java.util.HashSet;
import java.util.Set;

public class LanguagePack extends ConfigurationPack {
    public static final String TEMPLATE_DIR = "/templates/lang/%s.%s.yml";
    public static final String FILE_DIR = "%s/lang/%s/%s.yml";
    private final String locale;

    public LanguagePack(String id, String locale, Plugin provider) {
        super(id, provider);
        this.locale = locale;
        createTemplateFile();
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
        return "LanguagePack{id=%s,locale=%s}".formatted(this.id, this.locale);
    }

    @Override
    public String getRegisterId() {
        return getId() + "@" + locale;
    }

    @Override
    public String getRootSectionName() {
        return "language";
    }

    @Override
    public String getTemplateResource() {
        return "/templates/lang/%s.%s.yml".formatted(this.id, this.locale);
    }

    @Override
    public String getStorageFile() {
        return "/lang/%s/%s.yml".formatted(this.id, this.locale);
    }

    @Override
    public String getTemplateFile() {
        return "/lang/template/%s/%s.yml".formatted(this.id, this.locale);
    }
}
