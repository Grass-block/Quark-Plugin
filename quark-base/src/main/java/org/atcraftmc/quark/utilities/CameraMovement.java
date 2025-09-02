package org.atcraftmc.quark.utilities;

import me.gb2022.commons.math.LinearInterpolation;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.core.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SLModule(version = "1.0.0")
@QuarkCommand(name = "camera", permission = "-quark.camera", playerOnly = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CameraMovement extends CommandModule {

    public static final int INVALID_ROTATION = -1145141919;

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        MessageAccessor.send(this.getLanguage(), sender, "cam-start", args[0]);

        Player player = ((Player) sender);

        String tid = "quark:camera-" + UUID.randomUUID();

        int length = Integer.parseInt(args[0]);

        int x1;
        int y1;
        int z1;
        int x2;
        int y2;
        int z2;

        int xr2;
        int yr2;
        int xr1;
        int yr1;

        if (args.length > 7) {
            x1 = Integer.parseInt(args[1]);
            y1 = Integer.parseInt(args[2]);
            z1 = Integer.parseInt(args[3]);

            x2 = Integer.parseInt(args[6]);
            y2 = Integer.parseInt(args[7]);
            z2 = Integer.parseInt(args[8]);

            xr1 = Integer.parseInt(args[4]);
            yr1 = Integer.parseInt(args[5]);

            xr2 = Integer.parseInt(args[9]);
            yr2 = Integer.parseInt(args[10]);
        } else {
            x1 = Integer.parseInt(args[1]);
            y1 = Integer.parseInt(args[2]);
            z1 = Integer.parseInt(args[3]);
            x2 = Integer.parseInt(args[4]);
            y2 = Integer.parseInt(args[5]);
            z2 = Integer.parseInt(args[6]);

            xr2 = INVALID_ROTATION;
            yr2 = INVALID_ROTATION;
            xr1 = INVALID_ROTATION;
            yr1 = INVALID_ROTATION;
        }

        var useRotation = xr1 != INVALID_ROTATION;

        TaskService.global().timer(tid, 1, 1, new CameraTask(player, length, tid, useRotation, useRotation, (t) -> {
            var x = LinearInterpolation.do1(x1, x2, t);
            var y = LinearInterpolation.do1(y1, y2, t);
            var z = LinearInterpolation.do1(z1, z2, t);
            var xr = LinearInterpolation.do1(xr1, xr2, t);
            var yr = LinearInterpolation.do1(yr1, yr2, t);

            var tt = new Location(player.getWorld(), x, y, z);
            tt.setYaw((float) xr);
            tt.setPitch((float) yr);

            return tt;
        }));
    }

    public Location bezier(double t, Location start, Location end, Location... controls) {
        List<Location> points = new ArrayList<>();
        points.add(start);
        points.addAll(List.of(controls));
        points.add(end);

        while (points.size() > 1) {
            List<Location> newPoints = new ArrayList<>();
            for (int i = 0; i < points.size() - 1; i++) {
                Location p0 = points.get(i);
                Location p1 = points.get(i + 1);
                double x = (1 - t) * p0.getX() + t * p1.getX();
                double y = (1 - t) * p0.getY() + t * p1.getY();
                double z = (1 - t) * p0.getZ() + t * p1.getZ();
                newPoints.add(new Location(p0.getWorld(), x, y, z));
            }
            points = newPoints;
        }
        return points.get(0);
    }

    public static final class CameraTask implements Runnable {
        private final Player viewer;
        private final int ticks;
        private final Function<Double, Location> callback;
        private final String tid;
        private final Location lastLocation;
        private final GameMode lastGameMode;
        private final boolean applyRotation;

        private int tick = 0;

        public CameraTask(Player viewer, int ticks, String tid, boolean applyRotation, boolean alignRotation, Function<Double, Location> callback) {
            this.viewer = viewer;
            this.ticks = ticks;
            this.callback = callback;
            this.tid = tid;

            this.lastLocation = viewer.getLocation();
            this.lastGameMode = viewer.getGameMode();
            this.applyRotation = applyRotation;

            Location start = callback.apply(0d);

            if (!alignRotation) {
                start.setYaw(viewer.getLocation().getYaw());
                start.setPitch(viewer.getLocation().getPitch());
            }

            viewer.setGameMode(GameMode.SPECTATOR);

            viewer.teleport(start);
        }


        @Override
        public void run() {
            if (this.tick > this.ticks) {
                TaskService.global().cancel(this.tid);
                this.viewer.teleport(this.lastLocation);
                this.viewer.setGameMode(this.lastGameMode);
                return;
            }

            double t1 = (double) this.tick / this.ticks;
            double t2 = (double) (this.tick + 1) / this.ticks;

            Location interpolatedLocation = this.callback.apply(t1);
            Location nextInterpolatedLocation = this.callback.apply(t2);

            // 计算方向向量
            Vector direction = nextInterpolatedLocation.toVector().subtract(interpolatedLocation.toVector());

            // 设置玩家的速度
            this.viewer.setVelocity(direction.multiply(1));

            if (this.applyRotation) {
                try {
                    this.viewer.setRotation(interpolatedLocation.getYaw(), interpolatedLocation.getPitch());
                } catch (Exception ignored) {
                }
            }

            this.tick++;
        }
    }
}

