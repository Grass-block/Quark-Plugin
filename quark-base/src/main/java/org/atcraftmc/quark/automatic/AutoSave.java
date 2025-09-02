package org.atcraftmc.quark.automatic;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.TaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SLModule(version = "1.0.0", beta = true, compatBlackList = {APIProfile.FOLIA})
@CommandProvider(AutoSave.SaveWorldCommand.class)
public final class AutoSave extends PackageModule implements Runnable {
    private final Map<World, Boolean> worlds = new HashMap<>();

    @Override
    public void enable() {
        this.worlds.clear();
        var config = this.getConfig();

        for (String id : config.value("worlds").list(String.class)) {
            World world = Bukkit.getWorld(id);
            if (world == null) {
                continue;
            }
            this.worlds.put(world, world.isAutoSave());
            world.setAutoSave(false);
        }

        int delay = config.value("delay").intValue();
        int period = config.value("period").intValue();
        TaskService.global().timer("quark://auto_save/timer", delay, period, this);
    }

    @Override
    public void disable() {
        TaskService.global().cancel("quark://auto_save/timer");
        for (World world : this.worlds.keySet()) {
            world.setAutoSave(this.worlds.get(world));
        }
    }

    private void broadcast(World world, String msgId) {
        if (this.getConfig().value("silent").bool()) {
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() != world) {
                continue;
            }
            if (this.getConfig().value("broadcast-op-only").bool() && !p.isOp()) {
                continue;
            }
            this.getLanguage().item(msgId).send(p, world.getName());
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
                this.getLanguage().item("command").send(sender);
                for (World w : Bukkit.getWorlds()) {
                    this.getModule().saveWorld(w);
                }
                return;
            }
            this.getModule().saveWorld(Bukkit.getWorld(args[0]));
            this.getLanguage().item("command").send(sender);
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
