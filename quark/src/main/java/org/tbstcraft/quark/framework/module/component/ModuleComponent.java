package org.tbstcraft.quark.framework.module.component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;

public abstract class ModuleComponent<E extends AbstractModule> implements Listener {
    protected final E module;

    public ModuleComponent(final E module) {
        this.module = module;
    }

    protected LanguageEntry getLanguage() {
        return this.module.getLanguage();
    }

    protected ConfigurationSection getConfig() {
        return this.module.getConfig();
    }
}
