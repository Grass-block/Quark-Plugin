package org.tbstcraft.quark.utilities;

import me.gb2022.commons.math.LinearInterpolation;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.UUID;

@QuarkModule(version = "1.0.0")
@QuarkCommand(name = "camera", permission = "-quark.camera", playerOnly = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CameraMovement extends CommandModule {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        getLanguage().sendMessage(sender, "cam-start", args[0]);

        Player player = ((Player) sender);
        Location loc = player.getLocation();
        GameMode mode = player.getGameMode();

        player.setGameMode(GameMode.SPECTATOR);

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

            xr2 = Integer.parseInt(args[9]);
            yr2 = Integer.parseInt(args[10]);

            xr1 = Integer.parseInt(args[4]);
            yr1 = Integer.parseInt(args[5]);
        } else {
            x1 = Integer.parseInt(args[1]);
            y1 = Integer.parseInt(args[2]);
            z1 = Integer.parseInt(args[3]);
            x2 = Integer.parseInt(args[4]);
            y2 = Integer.parseInt(args[5]);
            z2 = Integer.parseInt(args[6]);

            xr2 = -1145141919;
            yr2 = -1145141919;
            xr1 = -1145141919;
            yr1 = -1145141919;
        }


        player.teleport(new Location(loc.getWorld(), x1, y1, z1));

        int finalXr = xr1;
        TaskService.timerTask(tid, 1, 1, new Runnable() {
            private int tick;

            @Override
            public void run() {
                if (this.tick > length) {
                    TaskService.cancelTask(tid);
                    getLanguage().sendMessage(sender, "cam-end");
                    player.setGameMode(mode);
                    player.teleport(loc);

                    return;
                }
                this.tick++;

                double t = (double) tick / length;
                double x = LinearInterpolation.do1(x1, x2, t);
                double y = LinearInterpolation.do1(y1, y2, t);
                double z = LinearInterpolation.do1(z1, z2, t);
                double xr = LinearInterpolation.do1(finalXr, xr2, t);
                double yr = LinearInterpolation.do1(yr1, yr2, t);

                double t2 = ((double) tick + 1f) / length;
                double xx = LinearInterpolation.do1(x1, x2, t2);
                double yy = LinearInterpolation.do1(y1, y2, t2);
                double zz = LinearInterpolation.do1(z1, z2, t2);

                if (finalXr != -1145141919) {
                    player.setRotation((float) xr, (float) yr);
                }
                player.setVelocity(new Vector((xx - x), (yy - y), (zz - z)));
            }
        });
    }
}

