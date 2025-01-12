package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.config.PackContainer;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.language.LanguageContainer;
import org.atcraftmc.qlib.language.LanguageEntry;

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
