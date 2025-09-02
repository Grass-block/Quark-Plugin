package org.atcraftmc.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

import java.util.Objects;

@SLModule(version = "1.0.0")
public final class AutoRunCommand extends PackageModule {

    @Override
    public void enable() {
        ConfigurationSection configs = Objects.requireNonNull(this.getConfig().value("commands").section());
        for (String name : configs.getKeys(false)) {
            ConfigurationSection command = configs.getConfigurationSection(name);
            if (command == null) {
                continue;
            }
            String commandLine = command.getString("command");
            int delay = command.getInt("delay");
            int period = command.getInt("period");

            String taskId = "quark://auto_run_command/" + name;
            TaskService.global().timer(taskId, delay, period, new CommandDispatchTask(commandLine));
        }
    }

    @Override
    public void disable() {
        ConfigurationSection configs = Objects.requireNonNull(this.getConfig().value("commands").section());
        for (String name : configs.getKeys(false)) {
            String taskId = "quark://auto_run_command/" + name;
            TaskService.global().cancel(taskId);
        }
    }

    private static final class CommandDispatchTask implements Runnable {
        private final String command;
        private final CommandSender sender = Bukkit.getConsoleSender();
        private final Server server = Bukkit.getServer();

        private CommandDispatchTask(String command) {
            this.command = command;
        }

        @Override
        public void run() {
            this.server.dispatchCommand(this.sender, this.command);
        }
    }
}
