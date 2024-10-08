package org.atcraftmc.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

@QuarkModule(version = "0.3", beta = true)
public final class AutoPluginReload extends PackageModule {
    @Override
    public void enable() {
        ConfigurationSection reloadItems = this.getConfig().getSection("reload-items");
        String template = this.getConfig().getString("reload-command");
        if (template == null) {
            return;
        }
        if (reloadItems == null) {
            return;
        }
        for (String s : reloadItems.getKeys(false)) {
            String tid = "plugin_reload:" + s;
            int delay = this.getConfig().getInt(s);
            TaskService.global().timer(tid, delay / 2, delay, new ReloadTask(template, s));
        }
    }

    @Override
    public void disable() {
        for (String s : TaskService.global().tasks()) {
            if (!s.startsWith("plugin_reload:")) {
                continue;
            }
            TaskService.global().cancel(s);
        }
    }

    private static final class ReloadTask implements Runnable {
        private final String commandLine;

        private ReloadTask(String template, String pluginName) {
            this.commandLine = template.replace("{plugin}", pluginName);
        }

        @Override
        public void run() {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), this.commandLine);
        }
    }
}
