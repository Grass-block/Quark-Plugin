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
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.util.TaskHandle;

import java.util.*;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(beta = true, compatBlackList = {APIProfile.ARCLIGHT, APIProfile.BUKKIT, APIProfile.BUKKIT})
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

            w.setTime((long) (w.getTime() + this.sleepingPlayers.get(w).size() * ConfigAccessor.getFloat(getConfig(), "scale-per-player")));
        }
    });

    @Inject("tip")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_TIP_PICK, (s) -> s.add(this.tip));
        TaskService.global().timer("quark:rs:health", 1, ConfigAccessor.getInt(this.getConfig(), "health-interval"), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!this.whateverSleepingPlayers.contains(p)) {
                    continue;
                }
                var health = p.getHealth() + ConfigAccessor.getFloat(getConfig(), "health-amount");
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
            MessageAccessor.send(this.getLanguage(), event.getPlayer(), "sleep-day");
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
            MessageAccessor.send(this.getLanguage(), sender, "leave-bed");
        }
    }
}
