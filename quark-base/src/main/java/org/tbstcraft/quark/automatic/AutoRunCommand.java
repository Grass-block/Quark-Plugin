package org.tbstcraft.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.Objects;

@QuarkModule(version = "1.0.0")
public final class AutoRunCommand extends PackageModule {

    @Override
    public void enable() {
        ConfigurationSection configs = Objects.requireNonNull(this.getConfig().getConfigurationSection("commands"));
        for (String name : configs.getKeys(false)) {
            ConfigurationSection command = configs.getConfigurationSection(name);
            if (command == null) {
                continue;
            }
            String commandLine = command.getString("command");
            int delay = command.getInt("delay");
            int period = command.getInt("period");

            String taskId = "quark://auto_run_command/" + name;
            TaskService.asyncTimerTask(taskId, delay, period, new CommandDispatchTask(commandLine));
        }
    }

    @Override
    public void disable() {
        ConfigurationSection configs = Objects.requireNonNull(this.getConfig().getConfigurationSection("commands"));
        for (String name : configs.getKeys(false)) {
            String taskId = "quark://auto_run_command/" + name;
            TaskService.cancelTask(taskId);
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
