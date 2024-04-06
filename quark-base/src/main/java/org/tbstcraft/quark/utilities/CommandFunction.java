package org.tbstcraft.quark.utilities;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

import java.util.List;

@QuarkModule(version = "1.0.0")
@EventListener
public class CommandFunction extends PackageModule {

    @Override
    public void enable() {
        for (String trigger : this.getConfig().getKeys(false)) {
            AbstractCommand command = new AdapterCommand(trigger, this.getConfig().getStringList(trigger));
            CommandManager.registerCommand(command);
        }
    }

    @Override
    public void disable() {
        for (String trigger : this.getConfig().getKeys(false)) {
            CommandManager.unregisterCommand(trigger);
        }
    }

    public static class AdapterCommand extends AbstractCommand {
        private final String trigger;
        private final List<String> triggerList;

        public AdapterCommand(String trigger, List<String> triggerList) {
            this.trigger = trigger;
            this.triggerList = triggerList;
        }

        public @NotNull String getName() {
            return this.trigger;
        }

        @Override
        public boolean isOP() {
            return false;
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            for (String pattern : triggerList) {
                for (int i = 0; i < args.length; i++) {
                    pattern = pattern.replace("{arg%d}".formatted(i), args[i]);
                }
                ((Player) sender).performCommand(pattern);
            }
        }
    }
}
