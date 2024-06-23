package org.tbstcraft.quark.foundation.command;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.language.LanguageEntry;

public abstract class CoreCommand extends AbstractCommand {
    private final LanguageEntry entry;

    protected CoreCommand() {
        this.init();
        this.entry = Quark.LANGUAGE.entry(this.getLanguageNamespace());
    }

    public LanguageEntry getLanguage() {
        return this.entry;
    }

    public String getLanguageNamespace() {
        return this.getName();
    }
}
