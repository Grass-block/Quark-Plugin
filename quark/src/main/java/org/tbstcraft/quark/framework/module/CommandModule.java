package org.tbstcraft.quark.framework.module;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.*;
import org.tbstcraft.quark.foundation.command.execute.CommandExecutor;


public abstract class CommandModule extends PackageModule implements CommandExecutor {
    private final AbstractCommand commandAdapter = new AdapterCommand<>(this);

    @Override
    public void enable() {
        CommandManager.registerQuarkCommand(this.commandAdapter);
    }

    @Override
    public void disable() {
        CommandManager.unregister(this.commandAdapter);
    }

    public Command getCoveredCommand() {
        return null;
    }

    public final Command getCovered() {
        return this.commandAdapter.getCovered();
    }

    public void sendExceptionMessage(CommandSender sender) {
        this.commandAdapter.sendExceptionMessage(sender);
    }

    public void sendPermissionMessage(CommandSender sender) {
        this.commandAdapter.sendPermissionMessage(sender, "(ServerOperator)");
    }

    public void sendPlayerOnlyMessage(CommandSender sender){
        this.commandAdapter.sendPlayerOnlyMessage(sender);
    }

    public static final class AdapterCommand<T extends CommandModule> extends ModuleCommand<T> {
        public AdapterCommand(T module) {
            super(module);
            this.init();
            this.setExecutor(module);
        }

        @Override
        public void init(T module) {
            this.setExecutor(module);
        }

        @Override
        public QuarkCommand getDescriptor() {
            return this.getModule().getClass().getAnnotation(QuarkCommand.class);
        }

        @Override
        public Command getCoveredCommand() {
            return this.getModule().getCoveredCommand();
        }
    }
}
