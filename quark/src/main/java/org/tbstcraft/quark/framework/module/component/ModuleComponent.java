package org.tbstcraft.quark.framework.module.component;

import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.FunctionalComponent;

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

    protected ConfigEntry getConfig() {
        return this.parent.getConfig();
    }
}
