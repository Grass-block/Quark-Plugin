package org.tbstcraft.quark.command;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.config.LanguageEntry;
import org.tbstcraft.quark.module.PluginModule;

public abstract class ModuleCommand<M extends PluginModule> extends AbstractCommand {
    private final M module;

    protected ModuleCommand(M module) {
        this.module = module;
        this.init();
    }

    public M getModule() {
        return module;
    }

    public LanguageEntry getLanguage() {
        return this.module.getLanguage();
    }

    public String getModuleId() {
        return this.getModule().getId();
    }

    protected ConfigurationSection getConfig() {
        return this.module.getConfig();
    }
}
