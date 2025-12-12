package org.atcraftmc.starlight.core.data.region;

import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

@ApplicationService(id = "region-position-provider")
public abstract class RegionPositionProvidingService implements SLService {
    public static final RegionPositionProvidingService PLAYER = new PlayerPositionProvider();
    public static final RegionPositionProvidingService SPAWN = new SpawnPositionProvider();
    private static final Set<RegionPositionProvidingService> INSTANCES = new HashSet<>();

    private final Set<RegionTrackService.RegionPositionTracker> targets = new HashSet<>();

    @ServiceInject
    static void start() {
        INSTANCES.add(PLAYER);
        INSTANCES.add(SPAWN);

        TaskService.async().timer("starlight:region", 0, 1, () -> {
            for (var rpt : INSTANCES) {
                rpt.tick();
            }
        });
    }

    @ServiceInject
    static void stop() {
        TaskService.async().cancel("starlight:region");
    }

    public static RegionPositionProvidingService player() {
        return PLAYER;
    }

    public static RegionPositionProvidingService spawn() {
        return SPAWN;
    }

    public void register(RegionTrackService.RegionPositionTracker provider) {
        this.targets.add(provider);
    }

    public void unregister(RegionTrackService.RegionPositionTracker provider) {
        this.targets.remove(provider);
    }

    public void activate(Location loc, int r) {
        for (var provider : this.targets) {
            provider.markLoad(loc, r);
        }
    }

    public abstract void tick();

    private static final class PlayerPositionProvider extends RegionPositionProvidingService {
        @Override
        public void tick() {
            for (var player : Bukkit.getOnlinePlayers()) {
                this.activate(player.getLocation(), RegionPos.LOAD_RADIUS);
            }
        }
    }

    private static final class SpawnPositionProvider extends RegionPositionProvidingService {
        @Override
        public void tick() {
            TaskService.global().run(() -> {
                for (var world : Bukkit.getWorlds()) {
                    this.activate(world.getSpawnLocation(), 4);
                }
            });
        }
    }
}
