package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.foundation.platform.FoliaServer;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.function.Consumer;

public final class FoliaGlobalTaskManager extends FoliaTaskManager {
    private final GlobalRegionScheduler scheduler;

    public FoliaGlobalTaskManager(Plugin owner) {
        super(owner);
        this.scheduler = FoliaServer.getGlobalScheduler();
    }

    @Override
    protected ScheduledTask runInternal(Consumer<ScheduledTask> action) {
        try {
            return this.scheduler.run(this.owner, action);
        } catch (Exception e) {
            TaskService.registerFinalizeTask(() -> action.accept(null));
        }
        return null;
    }

    @Override
    protected ScheduledTask delayInternal(long delay, Consumer<ScheduledTask> action) {
        return this.scheduler.runDelayed(this.owner, action, delay <= 0 ? 1 : delay);
    }

    @Override
    protected ScheduledTask timerInternal(long delay, long period, Consumer<ScheduledTask> action) {
        return this.scheduler.runAtFixedRate(this.owner, action, delay <= 0 ? 1 : delay, period);
    }
}
