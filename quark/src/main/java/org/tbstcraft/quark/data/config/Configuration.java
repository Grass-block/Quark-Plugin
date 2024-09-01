package org.tbstcraft.quark.data.config;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.ConfigurationPack;

public final class Configuration extends ConfigurationPack {

    public Configuration(Plugin provider, String id) {
        super(id, provider);
        createTemplateFile();
    }

    public Configuration(String id) {
        this(Quark.getInstance(), id);
    }


    @Override
    public String getRootSectionName() {
        return "config";
    }

    @Override
    public String getTemplateResource() {
        return "/templates/config/%s.yml".formatted(this.id);
    }

    @Override
    public String getStorageFile() {
        return "/config/%s.yml".formatted(this.id);
    }

    @Override
    public String getTemplateFile() {
        return "/config/template/%s.yml".formatted(this.id);
    }

    @Override
    public String toString() {
        return "Configuration{id=%s loader=%s}".formatted(this.id, this.provider.getClass().getName());
    }
}
