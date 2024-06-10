package org.tbstcraft.quark.framework.data.language;

import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.util.Identifiers;

import java.util.*;

public final class LanguageContainer {
    private final Map<Locale, LanguagePack> packs = new HashMap<>();
    private final String id;
    private final Plugin owner;

    private final LanguageAccess entry = new LanguageAccess(this);

    public LanguageContainer(String id, Plugin owner) {
        this.id = id;
        this.owner = owner;
        for (Locale loc : Locale.getAvailableLocales()) {
            if (!LanguagePack.existType(owner, id, loc)) {
                continue;
            }

            this.packs.put(loc, new LanguagePack(id, loc, owner));
        }
    }

    static String error(Locale locale, String namespace, String id) {
        return "ERROR: %s(%s/%s:%s)".formatted("PACK_NOT_FOUND", locale, namespace, id);
    }

    private LanguagePack getPack(Locale locale) {
        if (this.packs.containsKey(locale)) {
            return this.packs.get(locale);
        }
        return this.packs.get(Locale.SIMPLIFIED_CHINESE);
    }

    public void load() {
        for (LanguagePack pack : this.packs.values()) {
            pack.load();
        }
    }

    public void restore() {
        for (LanguagePack pack : this.packs.values()) {
            pack.restore();
        }
    }

    public void sync(boolean clean) {
        for (LanguagePack pack : this.packs.values()) {
            pack.sync(clean);
        }
    }

    public String getMessage(Locale locale, String namespace, String id) {
        LanguagePack pack = getPack(locale);
        if (pack == null) {
            return error(locale, Identifiers.external(namespace), Identifiers.external(id));
        }

        return pack.getMessage(Identifiers.external(namespace), Identifiers.external(id));
    }

    public List<String> getMessageList(Locale locale, String namespace, String id) {
        LanguagePack pack = getPack(locale);
        if (pack == null) {
            return Collections.singletonList(error(locale, Identifiers.external(namespace), Identifiers.external(id)));
        }

        return pack.getMessageList(Identifiers.external(namespace), Identifiers.external(id));
    }

    public String getId() {
        return id;
    }

    public Plugin getOwner() {
        return owner;
    }

    public Map<Locale, LanguagePack> getPacks() {
        return packs;
    }

    public LanguageAccess getEntry() {
        return entry;
    }



}
