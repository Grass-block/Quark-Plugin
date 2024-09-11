package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.foundation.platform.FoliaServer;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaAsyncTaskManager extends FoliaTaskManager {
    private final AsyncScheduler scheduler = FoliaServer.getAsyncScheduler();

    public FoliaAsyncTaskManager(Plugin owner) {
        super(owner);
    }

    @Override
    protected ScheduledTask runInternal(Consumer<ScheduledTask> action) {
        return this.scheduler.runNow(this.owner, action);
    }

    @Override
    protected ScheduledTask delayInternal(long delay, Consumer<ScheduledTask> action) {
        return this.scheduler.runDelayed(this.owner, action, delay * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    protected ScheduledTask timerInternal(long delay, long period, Consumer<ScheduledTask> action) {
        return this.scheduler.runAtFixedRate(this.owner, action, delay * 50, period * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cleanup(Plugin owner) {
        this.scheduler.cancelTasks(this.owner);
    }
}
