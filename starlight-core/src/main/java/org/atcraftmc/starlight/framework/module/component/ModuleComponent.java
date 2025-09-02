package org.atcraftmc.starlight.framework.module.component;

import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.module.AbstractModule;
import org.atcraftmc.starlight.framework.FunctionalComponent;

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
