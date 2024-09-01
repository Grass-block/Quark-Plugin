package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.regex.Pattern;

public final class ConfigEntry {
    private final ConfigAccess parent;
    private final String namespace;

    public ConfigEntry(ConfigAccess parent, String namespace) {
        this.parent = parent;
        this.namespace = namespace;
    }

    public <I> I get(String id, Class<I> type) {
        return this.parent.get(this.namespace, id, type);
    }

    public Object get(String id) {
        return this.parent.get(this.namespace, id);
    }

    public int getInt(String id) {
        return this.parent.getInt(this.namespace, id);
    }

    public float getFloat(String id) {
        return this.parent.getFloat(this.namespace, id);
    }

    public String getString(String id) {
        return this.parent.getString(this.namespace, id);
    }

    public List<String> getList(String id) {
        return this.parent.getList(this.namespace, id);
    }

    public ConfigurationSection getSection(String id) {
        return this.parent.getSection(this.namespace, id);
    }

    public boolean getBoolean(String id) {
        return this.parent.getBoolean(this.namespace, id);
    }

    public Pattern getRegex(String id) {
        return this.parent.getRegex(this.namespace, id);
    }

    public boolean isType(String id, Class<String> type) {
        return this.parent.isType(this.namespace, id, type);
    }
}
