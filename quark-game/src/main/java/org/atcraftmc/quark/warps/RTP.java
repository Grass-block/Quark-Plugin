package org.atcraftmc.quark.warps;

import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@QuarkModule
@QuarkCommand(name = "random-tp", aliases = "rtp", permission = "+quark.warps.rtp")
public final class RTP extends CommandModule {

    @Override
    public void execute(CommandExecution context) {
        getLanguage().sendMessage(context.getSender(), "start", this.getConfig().getInt("attempt"));

        this.teleport(context.requireSenderAsPlayer(), (loc) -> {
            if (loc == null) {
                getLanguage().sendMessage(context.getSender(), "failed");
                return;
            }

            getLanguage().sendMessage(context.getSender(), "success", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        });
    }


    public void attempt(Random random, Player player, Consumer<Location> callback, int counter, boolean async) {
        (async ? TaskService.async() : TaskService.global()).delay(1, () -> {

            var limit = this.getConfig().getInt("limit");
            var max = this.getConfig().getInt("max-height");
            var min = this.getConfig().getInt("min-height");
            var attempt = this.getConfig().getInt("attempt");

            var x = random.nextInt(-limit, limit);
            var z = random.nextInt(-limit, limit);

            var world = player.getWorld();


            ChunkSnapshot snapshot;

            if (async) {
                try {
                    snapshot = world.getChunkAtAsync(x >> 4, z >> 4).get().getChunkSnapshot();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            } else {
                snapshot = world.getChunkAt(x >> 4, z >> 4).getChunkSnapshot();
            }

            try {
                max = snapshot.getHighestBlockYAt(x & 15, z & 15) + 4;
            } catch (Throwable ignored) {
            }


            for (int y = max; y >= min; y--) {
                var b = snapshot.getBlockType(x & 15, y, z & 15);
                var b1 = snapshot.getBlockType(x & 15, y + 1, z & 15);
                var b2 = snapshot.getBlockType(x & 15, y + 2, z & 15);
                var b3 = snapshot.getBlockType(x & 15, y + 3, z & 15);

                if (!b.isSolid()) {
                    continue;
                }

                if (verifyAirBlock(b1)) {
                    continue;
                }

                if (verifyAirBlock(b2)) {
                    continue;
                }

                if (verifyAirBlock(b3)) {
                    continue;
                }

                Location loc = new Location(player.getWorld(), x, y, z);

                TaskService.global().run(() -> {
                    Players.teleport(player, loc.add(0.5, 1, 0.5));
                    callback.accept(loc);
                });

                return;
            }

            if (counter >= attempt) {
                callback.accept(null);
                return;
            }

            attempt(random, player, callback, counter + 1, async);
        });
    }

    public void teleport(Player player, Consumer<Location> callback) {
        try {
            World.class.getMethod("getChunkAtAsync", int.class, int.class);
            this.attempt(new Random(), player, callback, 0, true);
        } catch (NoSuchMethodException e) {
            getL4jLogger().warn("using legacy thread-sync finder. lags may occur.");
            this.attempt(new Random(), player, callback, 0, false);
        }
    }


    private boolean verifyAirBlock(Material b) {
        return !b.isAir();
    }
}
