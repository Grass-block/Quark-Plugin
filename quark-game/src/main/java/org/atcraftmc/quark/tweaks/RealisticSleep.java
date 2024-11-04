package org.atcraftmc.quark.tweaks;

import me.gb2022.commons.math.MathHelper;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.tbstcraft.quark.api.PluginMessages;
import org.tbstcraft.quark.api.PluginStorage;
import org.tbstcraft.quark.data.language.LanguageItem;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.platform.APIProfile;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.TaskHandle;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(beta = true, compatBlackList = {APIProfile.ARCLIGHT, APIProfile.BUKKIT, APIProfile.BUKKIT})
@CommandProvider(RealisticSleep.LeaveBedCommand.class)
public final class RealisticSleep extends PackageModule {
    private final Map<World, Set<Player>> sleepingPlayers = new HashMap<>();

    private final Set<Player> daySleepingPlayers = new HashSet<>();
    private final Set<Player> whateverSleepingPlayers = new HashSet<>();

    private final TaskHandle daemonTask = TaskHandle.timer(TaskService.global(), "quark:rs:daemon", 1, 1, () -> {
        for (World w : Bukkit.getWorlds()) {
            if (w.isDayTime()) {
                continue;
            }
            if (!this.sleepingPlayers.containsKey(w)) {
                this.sleepingPlayers.put(w, new HashSet<>());
            }

            w.setTime((long) (w.getTime() + this.sleepingPlayers.get(w).size() * this.getConfig().getFloat("scale-per-player")));
        }
    });

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        TaskService.global().timer("quark:rs:health", 1, this.getConfig().getInt("health-interval"), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!this.whateverSleepingPlayers.contains(p)) {
                    continue;
                }
                var health = p.getHealth() + this.getConfig().getFloat("health-amount");
                var max = Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

                p.setHealth(MathHelper.clamp(health, 0, max));
            }
        });

        this.daemonTask.start();
    }

    @Override
    public void disable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.remove(this.tip));
        TaskService.global().cancel("quark:rs:health");
        this.daemonTask.stop();
    }


    private Set<Player> getPlayerList(Player p) {
        if (!this.sleepingPlayers.containsKey(p.getWorld())) {
            this.sleepingPlayers.put(p.getWorld(), new HashSet<>());
        }
        return this.sleepingPlayers.get(p.getWorld());
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        event.setUseBed(Event.Result.ALLOW);
        this.whateverSleepingPlayers.add(event.getPlayer());
        if (event.getPlayer().getWorld().isDayTime()) {
            this.daySleepingPlayers.add(event.getPlayer());
            this.getLanguage().sendMessage(event.getPlayer(), "sleep-day");
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


    @QuarkCommand(name = "leave-bed", permission = "+quark.bed.leave", playerOnly = true, aliases = {"wakeup", "leave"})
    public static final class LeaveBedCommand extends ModuleCommand<RealisticSleep> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().daySleepingPlayers.remove(((Player) sender));
            this.getLanguage().sendMessage(sender, "leave-bed");
        }
    }
}
