package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.config.PackContainer;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.Starlight;

@QuarkCommand(name = "config", permission = "-quark.config")
public final class ConfigCommand extends PackConfigureCommand {

    @Override
    public LanguageEntry getLanguageEntry() {
        return Starlight.lang().entry("starlight-core", "config");
    }

    @Override
    public PackContainer<?> getPackContainer() {
        return ConfigContainer.getInstance();
    }
}
