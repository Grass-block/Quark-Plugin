package org.tbstcraft.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@QuarkModule(version = "1.0.0", beta = true, compatBlackList = {APIProfile.FOLIA})
@CommandProvider(AutoSave.SaveWorldCommand.class)
public final class AutoSave extends PackageModule implements Runnable {
    private final Map<World, Boolean> worlds = new HashMap<>();

    @Override
    public void enable() {
        this.worlds.clear();
        var config = this.getConfig();

        for (String id : config.getList("worlds")) {
            World world = Bukkit.getWorld(id);
            if (world == null) {
                continue;
            }
            this.worlds.put(world, world.isAutoSave());
            world.setAutoSave(false);
        }

        int delay = config.getInt("delay");
        int period = config.getInt("period");
        TaskService.timerTask("quark://auto_save/timer", delay, period, this);
    }

    @Override
    public void disable() {
        TaskService.cancelTask("quark://auto_save/timer");
        for (World world : this.worlds.keySet()) {
            world.setAutoSave(this.worlds.get(world));
        }
    }

    private void broadcast(World world, String msgId) {
        if (this.getConfig().getBoolean("silent")) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != world) {
                continue;
            }
            if (this.getConfig().getBoolean("broadcast-op-only") && !p.isOp()) {
                continue;
            }
            this.getLanguage().sendMessage(p, msgId, world.getName());
        }
    }

    public void saveWorld(World world) {
        this.broadcast(world, "restart");
        world.save();
        this.broadcast(world, "finish");
    }

    @Override
    public void run() {
        for (World world : this.worlds.keySet()) {
            saveWorld(world);
        }
    }

    @QuarkCommand(name = "save-world")
    public static final class SaveWorldCommand extends ModuleCommand<AutoSave> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (Objects.equals(args[0], "all")) {
                this.getLanguage().sendMessage(sender, "command");
                for (World w : Bukkit.getWorlds()) {
                    this.getModule().saveWorld(w);
                }
                return;
            }
            this.getModule().saveWorld(Bukkit.getWorld(args[0]));
            this.getLanguage().sendMessage(sender, "command");
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length != 1) {
                return;
            }
            for (World w : Bukkit.getWorlds()) {
                tabList.add(w.getName());
            }
            tabList.add("all");
        }
    }
}
