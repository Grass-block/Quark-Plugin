package org.tbstcraft.quark.contents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.util.api.APIProfile;

import java.util.*;

@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "1.0", compatBlackList = {APIProfile.ARCLIGHT, APIProfile.BUKKIT, APIProfile.BUKKIT})
@CommandRegistry(RealisticSleep.LeaveBedCommand.class)
public final class RealisticSleep extends PackageModule {
    private final Map<World, Set<Player>> sleepingPlayers = new HashMap<>();

    private final Set<Player> daySleepingPlayers = new HashSet<>();
    private final Set<Player> whateverSleepingPlayers = new HashSet<>();

    @Override
    public void enable() {
        TaskService.timerTask("quark:rs:health", 0, this.getConfig().getInt("health-interval"), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!this.whateverSleepingPlayers.contains(p)) {
                    continue;
                }
                double health = p.getHealth();
                health = health + this.getConfig().getDouble("health-amount");

                double max = Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

                if (health > max) {
                    health = max;
                }
                p.setHealth(health);
            }
        });

        TaskService.timerTask("quark:rs:daemon", 0, 1, () -> {
            for (World w : Bukkit.getWorlds()) {
                if (w.isDayTime()) {
                    continue;
                }
                if (!this.sleepingPlayers.containsKey(w)) {
                    this.sleepingPlayers.put(w, new HashSet<>());
                }

                w.setTime((long) (w.getTime() + this.sleepingPlayers.get(w).size() * this.getConfig().getDouble("scale-per-player")));
            }
        });
    }

    private Set<Player> getPlayerList(Player p) {
        if (!this.sleepingPlayers.containsKey(p.getWorld())) {
            this.sleepingPlayers.put(p.getWorld(), new HashSet<>());
        }
        return this.sleepingPlayers.get(p.getWorld());
    }


    @Override
    public void disable() {
        TaskService.cancelTask("quark:rs:daemon");
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        event.setUseBed(Event.Result.ALLOW);
        this.whateverSleepingPlayers.add(event.getPlayer());
        if (event.getPlayer().getWorld().isDayTime()) {
            this.daySleepingPlayers.add(event.getPlayer());
            this.getLanguage().sendMessageTo(event.getPlayer(), "sleep-day");
            return;
        }
        this.getPlayerList(event.getPlayer()).add(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeftBed(PlayerBedLeaveEvent event) {
        if (event.getPlayer().getWorld().isDayTime()) {
            if (!this.daySleepingPlayers.contains(event.getPlayer())) {
                this.whateverSleepingPlayers.remove(event.getPlayer());
                return;
            }
            event.setCancelled(true);
            return;
        }
        this.whateverSleepingPlayers.remove(event.getPlayer());
        this.getPlayerList(event.getPlayer()).remove(event.getPlayer());
    }


    @QuarkCommand(name = "leave-bed", permission = "+quark.bed.leave", playerOnly = true)
    public static final class LeaveBedCommand extends ModuleCommand<RealisticSleep> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().daySleepingPlayers.remove(((Player) sender));
            this.getLanguage().sendMessageTo(sender, "leave-bed");
        }
    }
}
