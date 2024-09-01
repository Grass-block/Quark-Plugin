package org.tbstcraft.quark.data.config;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.data.PackContainer;
import org.tbstcraft.quark.util.Identifiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ConfigContainer extends PackContainer<Configuration> {
    public static final ConfigContainer INSTANCE = new ConfigContainer();

    private final Map<String, Object> items = new HashMap<>();

    public static ConfigContainer getInstance() {
        return INSTANCE;
    }

    public static String key(String pack, String entry, String id) {
        return "%s:%s:%s".formatted(Identifiers.external(pack), Identifiers.external(entry), Identifiers.external(id));
    }

    private static String inlineKey(String k, String pack, String entry) {
        return switch (k.split(":").length) {
            case 1 -> pack + ":" + entry + ":" + k;
            case 2 -> pack + ":" + k;
            default -> k;
        };
    }


    public <I> I get(String pack, String entry, String id, Class<I> type) {
        return type.cast(this.items.get(key(pack, entry, id)));
    }

    public Object get(String pack, String entry, String id) {
        return this.items.get(key(pack, entry, id));
    }

    public Number getNumber(String pack, String entry, String id) {
        return get(pack, entry, id, Number.class);
    }

    public int getInt(String pack, String entry, String id) {
        return getNumber(pack, entry, id).intValue();
    }

    public float getFloat(String pack, String entry, String id) {
        return getNumber(pack, entry, id).floatValue();
    }

    public String getString(String pack, String entry, String id) {
        return get(pack, entry, id, String.class);
    }

    public List<String> getList(String pack, String entry, String id) {
        return get(pack, entry, id, List.class);
    }

    public boolean getBoolean(String pack, String entry, String id) {
        if (isType(pack, entry, id, String.class)) {
            return Boolean.parseBoolean(getString(pack, entry, id));
        }
        return get(pack, entry, id, Boolean.class);
    }

    public ConfigurationSection getSection(String pack, String entry, String id) {
        return get(pack, entry, id, ConfigurationSection.class);
    }

    public <I extends Enum<I>> Enum<I> getEnum(String pack, String entry, String id, Class<I> type) {
        for (var i = 0; i < type.getEnumConstants().length; i++) {
            if (type.getEnumConstants()[i].name().equals(getString(pack, entry, id))) {
                return type.getEnumConstants()[i];
            }
        }

        return null;
    }

    public Pattern getRegex(String pack, String entry, String id) {
        return Pattern.compile(getString(pack, entry, id));
    }

    public boolean isType(String pack, String entry, String id, Class<?> type) {
        return type.isInstance(get(pack, entry, id));
    }

    public Map<String, Object> getItems() {
        return items;
    }


    //----[Packs]----
    public void inject(Configuration pack) {
        for (var namespace : pack.getNamespaces()) {
            if (!pack.getRootSection().isConfigurationSection(namespace)) {
                continue;
            }

            var section = pack.getNamespace(namespace);

            for (var key : section.getKeys(false)) {
                var location = key(pack.getId(), namespace, key);

                if (section.isString(key)) {
                    this.items.put(location, section.getString(key));
                }
                if (section.isList(key)) {
                    this.items.put(location, section.getList(key));
                }
                this.items.put(location, section.get(key));
            }
        }
    }

    public void refresh(boolean clean) {
        if (clean) {
            this.items.clear();
        }
        super.refresh(clean);
    }

    //----[Access]----
    public ConfigAccess access(String pack) {
        return new ConfigAccess(this, pack);
    }

    public ConfigEntry entry(String pack, String entry) {
        return new ConfigEntry(access(pack), entry);
    }
}
