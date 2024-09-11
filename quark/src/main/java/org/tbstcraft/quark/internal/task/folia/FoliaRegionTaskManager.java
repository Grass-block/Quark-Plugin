package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.foundation.platform.FoliaServer;

import java.util.function.Consumer;

public final class FoliaRegionTaskManager extends FoliaTaskManager {
    private final RegionScheduler scheduler = FoliaServer.getRegionScheduler();

    private final Location location;

    public FoliaRegionTaskManager(Plugin owner, World world, int cx, int cz) {
        super(owner);
        this.location = new Location(world, cx * 16, 0, cz * 16);
    }

    @Override
    protected ScheduledTask runInternal(Consumer<ScheduledTask> action) {
        return this.scheduler.run(this.owner, this.location, action);
    }

    @Override
    protected ScheduledTask delayInternal(long delay, Consumer<ScheduledTask> action) {
        return this.scheduler.runDelayed(this.owner, this.location, action, delay <= 0 ? 1 : delay);
    }

    @Override
    protected ScheduledTask timerInternal(long delay, long period, Consumer<ScheduledTask> action) {
        return this.scheduler.runAtFixedRate(this.owner, this.location, action, delay <= 0 ? 1 : delay, period);
    }
}
