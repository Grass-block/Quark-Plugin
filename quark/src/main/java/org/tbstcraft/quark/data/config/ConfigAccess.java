package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.regex.Pattern;

public final class ConfigAccess {
    private final ConfigContainer parent;
    private final String pack;

    public ConfigAccess(ConfigContainer parent, String pack) {
        this.parent = parent;
        this.pack = pack;
    }

    public <I> I get(String entry, String id, Class<I> type) {
        return this.parent.get(this.pack, entry, id, type);
    }

    public Object get(String entry, String id) {
        return this.parent.get(this.pack, entry, id);
    }

    public int getInt(String entry, String id) {
        return this.parent.getInt(this.pack, entry, id);
    }

    public float getFloat(String entry, String id) {
        return this.parent.getFloat(this.pack, entry, id);
    }

    public String getString(String entry, String id) {
        return this.parent.getString(this.pack, entry, id);
    }

    public List<String> getList(String entry, String id) {
        return this.parent.getList(this.pack, entry, id);
    }

    public ConfigurationSection getSection(String entry, String id) {
        return this.parent.getSection(this.pack, entry, id);
    }

    public Pattern getRegex(String entry, String id) {
        return this.parent.getRegex(this.pack, entry, id);
    }

    public boolean getBoolean(String entry, String id) {
        return this.parent.getBoolean(this.pack, entry, id);
    }

    public boolean isType(String entry, String id, Class<String> type) {
        return this.parent.isType(this.pack, entry, id, type);
    }
}
