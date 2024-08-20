package org.tbstcraft.quark.framework.module.component;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.FunctionalComponent;

public abstract class ModuleComponent<E extends AbstractModule> implements FunctionalComponent {
    protected E parent;

    public ModuleComponent() {
    }

    public ModuleComponent(final E parent) {
        ctx(parent);
    }

    public void ctx(E parent) {
        this.parent = parent;
    }

    protected LanguageEntry getLanguage() {
        return this.parent.getLanguage();
    }

    protected ConfigurationSection getConfig() {
        return this.parent.getConfig();
    }
}
