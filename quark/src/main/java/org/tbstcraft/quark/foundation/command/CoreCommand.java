package org.tbstcraft.quark.foundation.command;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.tbstcraft.quark.Quark;
import org.atcraftmc.qlib.language.LanguageEntry;

public abstract class CoreCommand extends AbstractCommand {
    private final LanguageEntry entry;

    protected CoreCommand() {
        this.init(Quark.getInstance().getCommandManager());
        this.entry = Quark.LANGUAGE.entry(this.getLanguageNamespace());
    }

    public LanguageEntry getLanguage() {
        return this.entry;
    }

    public String getLanguageNamespace() {
        return this.getName();
    }
}
