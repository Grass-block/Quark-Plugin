package org.tbstcraft.quark.command;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.config.LanguageEntry;

public abstract class CoreCommand extends AbstractCommand {
    private final LanguageEntry entry;

    protected CoreCommand() {
        this.init();
        this.entry = Quark.LANGUAGE.createEntry(this.getLanguageNamespace());
    }

    public LanguageEntry getLanguage() {
        return this.entry;
    }

    public String getLanguageNamespace() {
        return this.getName();
    }
}
