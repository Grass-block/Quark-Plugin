package org.tbstcraft.quark.foundation.command;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.CommandManager;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.data.config.ConfigEntry;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.AbstractModule;

public abstract class ModuleCommand<M extends AbstractModule> extends AbstractCommand {
    private M module;

    public ModuleCommand(M module) {
        this.module = module;
    }

    protected ModuleCommand() {
        this.init(Quark.getInstance().getCommandManager());
    }

    @Override
    protected void init(CommandManager handle) {
        super.init(handle);
    }

    public void init() {
        init(QuarkCommandManager.getInstance());
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

    public void init(M module) {
    }

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

    public final ConfigEntry getConfig() {
        return this.module.getConfig();
    }
}
