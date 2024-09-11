package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.foundation.platform.FoliaServer;

import java.util.function.Consumer;

public final class FoliaEntityTaskManager extends FoliaTaskManager {
    private final EntityScheduler scheduler;

    public FoliaEntityTaskManager(Plugin owner, Entity entity) {
        super(owner);
        this.scheduler = FoliaServer.getEntityScheduler(entity);
    }

    @Override
    protected ScheduledTask runInternal(Consumer<ScheduledTask> action) {
        return this.scheduler.run(this.owner, action, null);
    }

    @Override
    protected ScheduledTask delayInternal(long delay, Consumer<ScheduledTask> action) {
        return this.scheduler.runDelayed(this.owner, action, null, delay <= 0 ? 1 : delay);
    }

    @Override
    protected ScheduledTask timerInternal(long delay, long period, Consumer<ScheduledTask> action) {
        return this.scheduler.runAtFixedRate(this.owner, action, null, delay <= 0 ? 1 : delay, period);
    }
}
