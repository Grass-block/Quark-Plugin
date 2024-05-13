package org.tbstcraft.quark.framework.module;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.*;

import java.util.List;


public abstract class CommandModule extends PackageModule implements CommandExecuter {
    private final AbstractCommand commandAdapter = new AdapterCommand<>(this);

    @Override
    public void enable() {
        CommandManager.registerCommand(this.commandAdapter);
    }

    @Override
    public void disable() {
        CommandManager.unregisterCommand(this.commandAdapter);
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
        this.commandAdapter.sendPermissionMessage(sender);
    }

    public static final class AdapterCommand<T extends CommandModule> extends ModuleCommand<T> {
        public AdapterCommand(T commandModule) {
            super(commandModule);
        }

        @Override
        public QuarkCommand getDescriptor() {
            return this.getModule().getClass().getAnnotation(QuarkCommand.class);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (this.getModule().execute(sender, args)) {
                return;
            }
            if (this.getModule().getCovered() == null) {
                return;
            }
            //this.getModule().getCovered().execute(sender, this.getLabel(), args);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            this.getModule().onTab(sender, buffer, tabList);
            if (this.getModule().getCovered() == null) {
                return;
            }
            //tabList.addAll(this.getModule().getCovered().tabComplete(sender, this.getLabel(), args));
        }

        @Override
        public Command getCoveredCommand() {
            return this.getModule().getCoveredCommand();
        }
    }
}
