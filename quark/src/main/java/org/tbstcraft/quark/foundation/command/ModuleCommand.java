package org.tbstcraft.quark.foundation.command;

import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;

public abstract class ModuleCommand<M extends AbstractModule> extends AbstractCommand {
    private M module;

    public ModuleCommand(M module) {
        this.module = module;
    }

    protected ModuleCommand() {
        this.init();
    }

    @SuppressWarnings({"rawtypes"})
    public final void initContext(M module) {
        this.module = module;

        for (AbstractCommand command : this.getSubCommands().values()) {
            if (command instanceof ModuleCommand moduleCommand) {
                moduleCommand.initContext((((ModuleCommand<?>) this).getModule()));
            }
        }

        this.init(module);
    }

    public void init(M module){}

    public final M getModule() {
        return module;
    }

    public final LanguageEntry getLanguage() {
        return this.module.getLanguage();
    }

    public final String getModuleId() {
        return this.getModule().getId();
    }

    public final String getModuleFullId() {
        return this.getModule().getFullId();
    }

    public final ConfigurationSection getConfig() {
        return this.module.getConfig();
    }
}
