package org.atcraftmc.quark.contents;

import me.gb2022.commons.container.MultiMap;
import me.gb2022.commons.math.MathHelper;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleO2;
import org.atcraftmc.qlib.language.Language;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;
import org.atcraftmc.starlight.core.PlayerView;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

//todo: test folia
@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule
@Components(RealisticMinecart.PlayerWorldCache.class)
public final class RealisticMinecart extends PackageModule {
    private static final String GLOBAL_TASK_ID = "quark:minecart:simulate";
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
        TaskService.global().timer(GLOBAL_TASK_ID, 1, 1, this::tick);

        for (var player : Bukkit.getOnlinePlayers()) {
            if (player.getVehicle() == null) {
                continue;
            }
            if (!(player.getVehicle() instanceof Minecart m)) {
                continue;
            }

            initUI(m, player);
        }
    }

    @Override
    public void disable() {
        TaskService.async().cancel(GLOBAL_TASK_ID);
    }

    @EventHandler
    public void onEnterMinecart(VehicleEnterEvent event) {
        if (!(event.getVehicle() instanceof Minecart m)) {
            return;
        }
        if (!(event.getEntered() instanceof Player p)) {
            return;
        }

        TaskService.entity(event.getEntered()).delay(1, () -> {
            var agent = VirtualMinecartAgent.get(this, p);

            if (playerWorldCache().isPlayerWarped(p)) {
                agent.bind(m);
                m.setVelocity(new Vector(0, 0, 0));
                m.setVelocity(buildInitialSpeedVector(p));
            } else {
                VirtualMinecartAgent.AGENTS.remove(p.getUniqueId());
                agent = VirtualMinecartAgent.get(this, p);
                p.getInventory().setHeldItemSlot(4);
                agent.setExpectedMaxSpeed(0);
                agent.setSpeedLimit(0);
            }

            playerWorldCache().updatePlayerWorld(p);
            initUI(m, p);
        });
    }

    @EventHandler
    public void onExitMinecart(VehicleExitEvent event) {
        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }
        if (!(event.getExited() instanceof Player p)) {
            return;
        }

        var view = PlayerView.getInstance(p).getActionbar();
        view.removeChannel("quark:realistic-minecart:ui");

        playerWorldCache().updatePlayerWorld(p);
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

    private void initUI(Minecart m, Player p) {
        var view = PlayerView.getInstance(p).getActionbar();

        view.addChannel("quark:realistic-minecart:ui", 999, 2, (a, t) -> {
            var thrustLevel = p.getInventory().getHeldItemSlot() - 4;
            var acceleration = ConfigAccessor.getFloat(getConfig(), "thrust-" + thrustLevel + "-acceleration");
            var speed = m.getMaxSpeed();

            this.renderUI(p, speed, acceleration, thrustLevel);
        });
    }

    private void renderUI(Player p, double speed, double acceleration, int thrustLevel) {
        var fmt = SharedObjects.NUMBER_FORMAT;
        var locale = LocaleService.locale(p);
        var template = Language.generateTemplate(this.getConfig(), "ui");
        var accelerationColumn = String.valueOf(fmt.format(acceleration * 20));
        var speedColumn = "%sm/s(%skm/h)".formatted(fmt.format(speed * 20), fmt.format(speed * 72));
        var thrustLevelColumn = String.valueOf(thrustLevel);
        var runMode = "";

        if (thrustLevel > 0) {
            runMode = MessageAccessor.getMessage(this.language, locale, "run-mode-boost");
        } else if (thrustLevel == 0) {
            runMode = MessageAccessor.getMessage(this.language, locale, "run-mode-run");
        } else {
            runMode = MessageAccessor.getMessage(this.language, locale, "run-mode-break");
        }

        if (speed == 0) {
            runMode = MessageAccessor.getMessage(this.language, locale, "run-mode-stop");
        }

        if (speed == ConfigAccessor.getFloat(getConfig(), "max-speed")) {
            runMode = MessageAccessor.getMessage(this.language, locale, "run-mode-run");
        }

        template = template.replace("{run-mode}", runMode);

        template = template.replace("{speed}", speedColumn)
                .replace("{acceleration}", accelerationColumn)
                .replace("{level}", thrustLevelColumn);

        String message = MessageAccessor.buildTemplate(this.language, locale, template);
        TextSender.sendActionbarTitle(p, TextBuilder.build(message));
    }

    private void tick() {
        /*
        for (var world : Bukkit.getWorlds()) {
            for (Minecart minecart : world.getEntitiesByClass(Minecart.class)) {
                if (minecart.getType() != EntityType.MINECART) {
                    continue;
                }

                TaskService.entity(minecart).run((ctx) -> VirtualMinecartAgent.get(this, minecart).tick());
            }
        }
         */

        for (var p : Bukkit.getOnlinePlayers()) {
            tickPlayerAndMinecart(p);
        }
    }

    private void tickPlayerAndMinecart(Player p) {
        if (!(p.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        TaskService.entity(minecart).run(() -> {
            var agent = VirtualMinecartAgent.get(this, p);
            agent.tick();
            var thrustLevel = p.getInventory().getHeldItemSlot() - 4;
            var acceleration = ConfigAccessor.getFloat(getConfig(), "thrust-" + thrustLevel + "-acceleration");

            agent.setSpeedLimit(ConfigAccessor.getFloat(getConfig(), "max-speed"));
            agent.setAcceleration(acceleration / 20f);

            if (agent.expectedMaxSpeed == 0 && thrustLevel <= 0) {
                minecart.setVelocity(new Vector(0, 0, 0));
            }
            if (BukkitUtil.getMaximumAxis(minecart.getVelocity()) == 0 && thrustLevel > 0) {
                minecart.setVelocity(this.buildInitialSpeedVector(p));
            }
        });
    }

    private PlayerWorldCache playerWorldCache() {
        return getComponent(PlayerWorldCache.class);
    }

    private Vector buildInitialSpeedVector(Player p) {
        var rotation = p.getLocation().getDirection();
        var vector = new Vector(0, 0, 0);
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

    @AutoRegister(Registers.BUKKIT_EVENT)
    public static class PlayerWorldCache extends ModuleComponent<RealisticMinecart> {
        private final Map<UUID, World> playerWorldTable = new HashMap<>();

        @Override
        public void enable() {
            for (var player : Bukkit.getOnlinePlayers()) {
                this.updatePlayerWorld(player);
            }
        }

        @Override
        public void disable() {
            this.playerWorldTable.clear();
        }

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            this.updatePlayerWorld(event.getPlayer());
        }

        @EventHandler
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            if (!(event.getPlayer().getVehicle() instanceof Minecart)) {
                this.updatePlayerWorld(event.getPlayer());
            }
        }

        public boolean isPlayerWarped(Player player) {
            if (!this.playerWorldTable.containsKey(player.getUniqueId())) {
                return false;
            }

            return player.getWorld() != this.playerWorldTable.get(player.getUniqueId());
        }

        public void updatePlayerWorld(Player player) {
            this.playerWorldTable.put(player.getUniqueId(), player.getWorld());
        }
    }

    public static final class VirtualMinecartAgent {
        public static final MultiMap<UUID, VirtualMinecartAgent> AGENTS = new MultiMap<>();

        private final RealisticMinecart holder;

        private Minecart minecart;
        private Location lastLocation;
        private boolean lastRailed;
        private Vector lastRecordedVelocity;
        private double expectedMaxSpeed;
        private double acceleration = 0.15f;
        private double speedLimit = 0.4f;

        private double lastYaw = 0;
        private double lastPitch = 0;

        public VirtualMinecartAgent(RealisticMinecart holder, Minecart minecart) {
            this.holder = holder;
            this.minecart = minecart;
            this.expectedMaxSpeed = minecart.getMaxSpeed();
        }

        static VirtualMinecartAgent get(RealisticMinecart holder, Player player) {
            return AGENTS.computeIfAbsent(
                    player.getUniqueId(),
                    m -> new VirtualMinecartAgent(holder, (Minecart) Objects.requireNonNull(player.getVehicle()))
            );
        }

        private static boolean isRail(Material material) {
            return material == Material.RAIL || material == Material.ACTIVATOR_RAIL || material == Material.POWERED_RAIL || material == Material.DETECTOR_RAIL;
        }

        public void bind(Minecart minecart) {
            this.minecart = minecart;
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

            if (velocity.length() > 0 && this.holder.getConfig().value("auto-align").bool()) {
                var yaw = Math.atan2(-velocity.getX(), velocity.getZ()) * (180 / Math.PI);
                var pitch = Math.atan2(
                        -velocity.getY(),
                        Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ())
                ) * (180 / Math.PI);

                if (this.lastYaw != yaw || this.lastPitch != pitch) {
                    for (Entity e : minecart.getPassengers()) {
                        SET_ROTATION.invoke(e, (float) yaw, (float) pitch);
                    }

                    this.lastYaw = yaw;
                    this.lastPitch = pitch;
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