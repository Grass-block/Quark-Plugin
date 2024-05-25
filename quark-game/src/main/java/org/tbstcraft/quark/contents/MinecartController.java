package org.tbstcraft.quark.contents;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.base.task.TaskService;
import org.tbstcraft.quark.util.api.BukkitUtil;
import org.tbstcraft.quark.util.api.PlayerUtil;

import java.text.DecimalFormat;
import java.util.HashSet;

@ModuleService(ServiceType.EVENT_LISTEN)
@QuarkModule
public final class MinecartController extends PackageModule {
    private final HashSet<Player> speeds = new HashSet<>();

    private static String getTaskIdentifier(Player p) {
        return "quark://minecart_controller/speed_calc/%s".formatted(p.getName());
    }

    @Override
    public void disable() {
        for (Player p : this.speeds) {
            TaskService.cancelTask(getTaskIdentifier(p));
        }
    }

    @EventHandler
    public void onUseMinecart(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart m)) {
            return;
        }
        if (event.getEntered() instanceof Player p) {
            p.getInventory().setHeldItemSlot(4);
            this.speeds.add(p);
            TaskService.timerTask(getTaskIdentifier(p), p, 1, 2, () -> this.tickPlayerMinecart(p));
            m.setMaxSpeed(0.01f);
        }
    }

    @EventHandler
    public void onLeaveMinecart(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }
        if (event.getExited() instanceof Player p) {
            TaskService.cancelTask(getTaskIdentifier(p));
            this.speeds.remove(p);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!this.speeds.contains(event.getPlayer())) {
            return;
        }
        this.speeds.remove(event.getPlayer());
        TaskService.cancelTask(getTaskIdentifier(event.getPlayer()));
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (!(event.getPlayer().getVehicle() instanceof Minecart)) {
            return;
        }
        if (event.getPreviousSlot() == 0 && event.getNewSlot() == 8) {
            event.setCancelled(true);
        }
        if (event.getPreviousSlot() == 8 && event.getNewSlot() == 0) {
            event.setCancelled(true);
        }
    }

    private void tickPlayerMinecart(Player p) {
        Minecart minecart = (Minecart) p.getVehicle();
        if (minecart == null) {
            return;
        }

        int thrustLevel = p.getInventory().getHeldItemSlot() - 4;
        double acceleration = getConfig().getDouble("thrust-" + thrustLevel + "-acceleration");

        double speed;
        speed = minecart.getMaxSpeed();

        speed += acceleration / 10f;
        if (speed > getConfig().getDouble("max-speed")) {
            speed = getConfig().getDouble("max-speed");
        }
        if (speed < 0) {
            speed = 0;
        }
        minecart.setMaxSpeed(speed);

        if (speed == 0 && thrustLevel <= 0) {
            minecart.setVelocity(new Vector(0, 0, 0));
        }
        if (BukkitUtil.getMaximumAxis(minecart.getVelocity()) == 0 && thrustLevel > 0) {
            minecart.setVelocity(this.buildPlayerSpeedVector(p));
        }

        this.buildPlayerUI(p, speed, acceleration, thrustLevel);
    }

    private void buildPlayerUI(Player p, double speed, double acceleration, int thrustLevel) {
        DecimalFormat fmt = SharedObjects.NUMBER_FORMAT;
        String accelerationColumn = String.valueOf(fmt.format(acceleration * 20));
        String speedColumn = "%sm/s(%skm/h)".formatted(fmt.format(speed * 20), fmt.format(speed * 72));
        String thrustLevelColumn = String.valueOf(thrustLevel);
        String locale = Language.getLocale(p);
        String ui = this.getLanguage().buildUI(this.getConfig(), "ui", locale, (s) -> {
                    String s2;
                    if (thrustLevel > 0) {
                        s2 = this.getLanguage().getMessage(locale, "run-mode-boost");
                    } else if (thrustLevel == 0) {
                        s2 = this.getLanguage().getMessage(locale, "run-mode-run");
                    } else {
                        s2 = this.getLanguage().getMessage(locale, "run_mode_break");
                    }
                    if (speed == 0) {
                        s2 = this.getLanguage().getMessage(locale, "run-mode-stop");
                    }
                    if (speed == getConfig().getDouble("max-speed")) {
                        s2 = this.getLanguage().getMessage(locale, "run-mode-run");
                    }
                    return s.replace("{run-mode}", s2);
                }).replace("{speed}", speedColumn)
                .replace("{acceleration}", accelerationColumn)
                .replace("{level}", thrustLevelColumn);
        PlayerUtil.sendActionBarTitle(p, ui);
    }

    private Vector buildPlayerSpeedVector(Player p) {
        Vector rotation = p.getLocation().getDirection();
        Vector vector = new Vector(0, 0, 0);
        if (rotation.getZ() > 0) {
            vector.setZ(1);
        } else {
            vector.setZ(-1);
        }
        if (rotation.getX() > 0) {
            vector.setX(1);
        } else {
            vector.setX(-1);
        }
        return vector;
    }
}
