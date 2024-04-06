package org.tbstcraft.quark.module.compat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.config.LanguageEntry;
import org.tbstcraft.quark.module.AbstractModule;

public abstract class CompatContainer<V extends AbstractModule> implements Listener {
    private final V parent;

    public CompatContainer(V parent) {
        this.parent = parent;
    }

    public LanguageEntry getLanguage() {
        return this.parent.getLanguage();
    }

    public void init() {
    }

    protected V getParent() {
        return this.parent;
    }

    protected ConfigurationSection getConfig() {
        return this.parent.getConfig();
    }
}
