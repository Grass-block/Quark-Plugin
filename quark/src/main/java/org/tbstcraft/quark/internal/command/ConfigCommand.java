package org.tbstcraft.quark.internal.command;

import org.tbstcraft.quark.data.PackContainer;
import org.tbstcraft.quark.data.config.ConfigContainer;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.QuarkCommand;

@QuarkCommand(name = "config", permission = "-quark.config")
public final class ConfigCommand extends PackConfigureCommand {

    @Override
    public LanguageEntry getLanguageEntry() {
        return LanguageContainer.INSTANCE.entry("quark-core", "config");
    }

    @Override
    public PackContainer<?> getPackContainer() {
        return ConfigContainer.getInstance();
    }
}
