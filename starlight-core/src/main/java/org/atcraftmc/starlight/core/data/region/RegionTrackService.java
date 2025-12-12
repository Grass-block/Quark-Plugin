package org.atcraftmc.starlight.core.data.region;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RegionTrackService implements SLService {
    private static final Map<String, RegionPositionTracker> instances = new HashMap<>();

    @ServiceInject
    static void start() {
        TaskService.global().run(() -> {
            for (var rpt : instances.values()) {
                rpt.tick();
            }
        });
    }

    @ServiceInject
    static void stop() {

    }


    public static RegionTrackService getInstance() {
        throw new UnsupportedOperationException();
    }


    public static final class RegionPositionTracker extends RegionTrackService {
        private final Long2IntMap remainLifetime = new Long2IntOpenHashMap(RegionPos.MAX_PROVIDER_CAPACITY);
        private final Set<RegionLoadHandler> handlers = new HashSet<>();

        public void register(RegionLoadHandler handler) {
            for (var r : this.remainLifetime.keySet()) {
                handler.load(RegionPos.getX(r), RegionPos.getZ(r));
            }
            this.handlers.add(handler);
        }

        public void unregister(RegionLoadHandler handler) {
            this.handlers.remove(handler);
            for (var r : this.remainLifetime.keySet()) {
                handler.unload(RegionPos.getX(r), RegionPos.getZ(r));
            }
        }

        public void markLoad(Location location, int radius) {
            var cx = location.getBlockX() / RegionPos.REGION_ASPECT;
            var cz = location.getBlockZ() / RegionPos.REGION_ASPECT;

            for (var ox = -radius; ox <= radius; ox++) {
                for (var oz = -radius; oz <= radius; oz++) {
                    var k = RegionPos.encode(cx + ox, cz + oz);
                    if (!this.remainLifetime.containsKey(k)) {
                        for (var h : this.handlers) {
                            h.load(cx + ox, cz + oz);
                        }
                    }
                    this.remainLifetime.put(k, RegionPos.LIFETIME_INCREMENT);
                }
            }
        }

        public void tick() {
            for (var k : new LongArraySet(remainLifetime.keySet())) {
                if (this.remainLifetime.get(k) <= 0) {
                    this.remainLifetime.remove(k);
                    for (var h : this.handlers) {
                        h.unload(RegionPos.getX(k), RegionPos.getZ(k));
                    }
                }

                this.remainLifetime.put(((long) k), this.remainLifetime.get(k) - 1);
            }
        }
    }
}
