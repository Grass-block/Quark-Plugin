package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.config.PackContainer;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.language.LanguageEntry;

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
