package org.tbstcraft.quark.internal.command;

import org.tbstcraft.quark.data.PackContainer;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

@QuarkCommand(name = "language", permission = "-quark.language")
public final class LanguageCommand extends PackConfigureCommand {

    @Override
    public LanguageEntry getLanguageEntry() {
        return LanguageContainer.INSTANCE.entry("quark-core", "language");
    }

    @Override
    public PackContainer<?> getPackContainer() {
        return LanguageContainer.INSTANCE;
    }
}
