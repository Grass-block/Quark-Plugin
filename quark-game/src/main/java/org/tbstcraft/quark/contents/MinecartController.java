package org.tbstcraft.quark.contents;

import me.gb2022.commons.container.MultiMap;
import me.gb2022.commons.math.MathHelper;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule
public final class MinecartController extends PackageModule {
    private static final String TASK_ID = "quark:minecart:simulate";
    private static final double MAX_SAFE_SPEED = 0.6;
    private static final double SIMULATED_GRAVITY = 0.05;
    private static final MethodHandleO2<Entity, Float, Float> SET_ROTATION = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> Entity.class.getMethod("setRotation", float.class, float.class), (e, y, p) -> {
            try {
                e.setRotation(y, p);
            } catch (UnsupportedOperationException ignored) {
            }
        });
        ctx.dummy((e, y, p) -> {
        });
    });

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        TaskService.timerTask(TASK_ID, 1, 1, this::tick);
    }

    @Override
    public void disable() {
        TaskService.cancelTask(TASK_ID);
    }

    @EventHandler
    public void onUseMinecart(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart m)) {
            return;
        }
        if (event.getEntered() instanceof Player p) {
            p.getInventory().setHeldItemSlot(4);

            var agent = VirtualMinecartAgent.AGENTS.get(m);

            agent.setExpectedMaxSpeed(0);
            agent.setSpeedLimit(0);
        }
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
        if (!(p.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        var agent = VirtualMinecartAgent.get(minecart);
        var thrustLevel = p.getInventory().getHeldItemSlot() - 4;
        var acceleration = getConfig().getDouble("thrust-" + thrustLevel + "-acceleration");
        var speed = minecart.getMaxSpeed();

        agent.setSpeedLimit(getConfig().getDouble("max-speed"));
        agent.setAcceleration(acceleration / 20f);

        if (agent.expectedMaxSpeed == 0 && thrustLevel <= 0) {
            minecart.setVelocity(new Vector(0, 0, 0));
        }
        if (BukkitUtil.getMaximumAxis(minecart.getVelocity()) == 0 && thrustLevel > 0) {
            minecart.setVelocity(this.buildPlayerSpeedVector(p));
        }

        this.renderUI(p, speed, acceleration, thrustLevel);
    }

    private void renderUI(Player p, double speed, double acceleration, int thrustLevel) {
        var fmt = SharedObjects.NUMBER_FORMAT;
        var locale = Language.locale(p);
        var template = Language.generateTemplate(this.getConfig(), "ui");

        var accelerationColumn = String.valueOf(fmt.format(acceleration * 20));
        var speedColumn = "%sm/s(%skm/h)".formatted(fmt.format(speed * 20), fmt.format(speed * 72));
        var thrustLevelColumn = String.valueOf(thrustLevel);

        var runMode = "";
        if (thrustLevel > 0) {
            runMode = this.language.getMessage(locale, "run-mode-boost");
        } else if (thrustLevel == 0) {
            runMode = this.language.getMessage(locale, "run-mode-run");
        } else {
            runMode = this.language.getMessage(locale, "run_mode_break");
        }
        if (speed == 0) {
            runMode = this.language.getMessage(locale, "run-mode-stop");
        }
        if (speed == getConfig().getDouble("max-speed")) {
            runMode = this.language.getMessage(locale, "run-mode-run");
        }
        template = template.replace("{run-mode}", runMode);

        template = template.replace("{speed}", speedColumn)
                .replace("{acceleration}", accelerationColumn)
                .replace("{level}", thrustLevelColumn);

        Players.sendActionBarTitle(p, this.language.buildTemplate(locale, template));
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

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            tickPlayerMinecart(p);
        }

        for (World world : Bukkit.getWorlds()) {
            for (Minecart minecart : world.getEntitiesByClass(Minecart.class)) {
                if (minecart.getType() != EntityType.MINECART) {
                    continue;
                }

                VirtualMinecartAgent.get(minecart).tick();
            }
        }
    }


    public static final class VirtualMinecartAgent {
        public static final MultiMap<Minecart, VirtualMinecartAgent> AGENTS = new MultiMap<>();

        private final Minecart minecart;
        private Location lastLocation;
        private boolean lastRailed;
        private Vector lastRecordedVelocity;
        private double expectedMaxSpeed = 0f;
        private double acceleration = 0.15f;
        private double speedLimit = 0.4f;

        public VirtualMinecartAgent(Minecart minecart) {
            this.minecart = minecart;
        }

        static VirtualMinecartAgent get(Minecart minecart) {
            return AGENTS.computeIfAbsent(minecart, VirtualMinecartAgent::new);
        }

        private static boolean isRail(Material material) {
            return material == Material.RAIL || material == Material.ACTIVATOR_RAIL || material == Material.POWERED_RAIL || material == Material.DETECTOR_RAIL;
        }

        public void tick() {
            this.expectedMaxSpeed = MathHelper.clamp(this.expectedMaxSpeed + acceleration, 0, this.speedLimit);

            var loc = this.minecart.getLocation();

            if (this.lastLocation == null) {
                this.lastLocation = loc;
            }

            var dx = loc.getX() - this.lastLocation.getX();
            var dy = loc.getY() - this.lastLocation.getY();
            var dz = loc.getZ() - this.lastLocation.getZ();


            var b1 = minecart.getLocation().subtract(0, 1, 0).getBlock();
            var b2 = minecart.getLocation().getBlock();

            var railed = isRail(b1.getType()) || isRail(b2.getType());
            var velocity = new Vector(dx, dy, dz);

            if (velocity.length() > 0) {
                var yaw = Math.atan2(-velocity.getX(), velocity.getZ()) * (180 / Math.PI);
                var pitch = Math.atan2(
                        -velocity.getY(),
                        Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ())
                                      ) * (180 / Math.PI);

                for (Entity e : minecart.getPassengers()) {
                    SET_ROTATION.invoke(e, (float) yaw, (float) pitch);
                }
            }

            if (minecart.isOnGround()) {
                return;
            }

            if (this.lastRailed && !railed) {
                this.lastRecordedVelocity = velocity.multiply(new Vector(1, 1.5, 1));
            }

            if (!railed) {
                if (this.lastRecordedVelocity == null) {
                    this.lastRecordedVelocity = velocity;
                }

                this.lastRecordedVelocity = this.lastRecordedVelocity.subtract(new Vector(0, SIMULATED_GRAVITY, 0));
                this.minecart.setVelocity(this.lastRecordedVelocity);
            }

            var shouldSlow = shouldSlow();

            this.minecart.setMaxSpeed(shouldSlow ? Math.min(MAX_SAFE_SPEED, this.expectedMaxSpeed) : this.expectedMaxSpeed);

            this.lastLocation = loc;
            this.lastRailed = railed;
        }

        public boolean shouldSlow() {
            int r = (int) (10 * MathHelper.clamp(minecart.getMaxSpeed(), 0.4, 1));

            var x = this.minecart.getLocation().getBlockX();
            var y = this.minecart.getLocation().getBlockY();
            var z = this.minecart.getLocation().getBlockZ();

            for (var xx = x - r; xx <= x + r; xx++) {
                for (var zz = z - r; zz <= z + r; zz++) {

                    var loc = new Location(this.minecart.getWorld(), xx, y, zz);

                    var block = loc.getBlock();

                    if (block.getType() == Material.RAIL) {
                        return true;
                    }

                    BlockData data = block.getBlockData();
                    if (isRail(block.getType())) {
                        var rail = ((Rail) data);

                        if (rail.getShape() == Rail.Shape.ASCENDING_NORTH || rail.getShape() == Rail.Shape.ASCENDING_SOUTH || rail.getShape() == Rail.Shape.ASCENDING_EAST || rail.getShape() == Rail.Shape.ASCENDING_WEST) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        public void setAcceleration(double acceleration) {
            this.acceleration = acceleration;
        }

        public void setSpeedLimit(double speedLimit) {
            this.speedLimit = speedLimit;
        }

        public void setExpectedMaxSpeed(double expectedMaxSpeed) {
            this.expectedMaxSpeed = expectedMaxSpeed;
        }
    }
}