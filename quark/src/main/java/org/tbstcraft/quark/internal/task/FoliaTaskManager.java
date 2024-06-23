package org.tbstcraft.quark.internal.task;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.foundation.platform.FoliaUtil;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public final class FoliaTaskManager extends TaskManager {
    private final RegionScheduler region = FoliaUtil.getRegionScheduler();
    private final AsyncScheduler async = FoliaUtil.getAsyncScheduler();

    public FoliaTaskManager(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void run(Location loc, Runnable task) {
        this.region.execute(this.getPlugin(), loc, task);
    }

    @Override
    public void delay(String id, Location loc, long delay, Runnable task) {
        FoliaTaskWrapper wrapper = new FoliaTaskWrapper(id, task, this);
        Task t = new FoliaTask(this.region.runDelayed(this.getPlugin(), loc, wrapper, delay));
        this.register(id, t);
    }

    @Override
    public void timer(String id, Location loc, long delay, long period, Runnable task) {
        this.region.runAtFixedRate(this.getPlugin(), loc, scheduledTask -> task.run(), delay, period);
    }

    @Override
    public void async(String id, Runnable task) {
        try {
            FoliaTaskWrapper wrapper = new FoliaTaskWrapper(id, task, this);
            Task t = new FoliaTask(this.async.runNow(this.getPlugin(), wrapper));
            this.register(id, t);
        } catch (Exception e) {
            SharedObjects.SHARED_THREAD_POOL.submit(task);
        }
    }

    @Override
    public void asyncDelay(String id, long delay, Runnable task) {
        FoliaTaskWrapper wrapper = new FoliaTaskWrapper(id, task, this);
        ScheduledTask t = this.async.runDelayed(this.getPlugin(), wrapper, delay * 50, TimeUnit.MILLISECONDS);
        this.register(id, new FoliaTask(t));
    }

    @Override
    public void asyncTimer(String id, long delay, long period, Runnable task) {
        ScheduledTask t = this.async.runAtFixedRate(this.getPlugin(), scheduledTask -> task.run(),
                delay * 50, period * 50, TimeUnit.MILLISECONDS);
        this.register(id, new FoliaTask(t));
    }

    @Override
    public void run(Entity entity, Runnable task) {
        FoliaUtil.getEntityScheduler(entity).run(this.getPlugin(), scheduledTask -> task.run(), () -> {
        });
    }

    @Override
    public void delay(String id, Entity entity, long delay, Runnable task) {
        ScheduledTask t = FoliaUtil.getEntityScheduler(entity).runDelayed(this.getPlugin(), scheduledTask -> task.run(), () -> unregister(id), delay);
        this.register(id, new FoliaTask(t));
    }

    @Override
    public void timer(String id, Entity entity, long delay, long period, Runnable task) {
        ScheduledTask t = FoliaUtil.getEntityScheduler(entity).runAtFixedRate(this.getPlugin(), scheduledTask -> task.run(), () -> unregister(id), delay, period);
        this.register(id, new FoliaTask(t));
    }

    private static final class FoliaTaskWrapper implements Consumer<ScheduledTask> {
        private final String id;
        private final Runnable task;
        private final FoliaTaskManager parent;

        private FoliaTaskWrapper(String id, Runnable task, FoliaTaskManager parent) {
            this.id = id;
            this.task = task;
            this.parent = parent;
        }

        @Override
        public void accept(ScheduledTask scheduledTask) {
            this.task.run();
            this.parent.unregister(this.id);
        }
    }

    private static final class FoliaTask implements Task {
        private final ScheduledTask task;

        private FoliaTask(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public Plugin getOwner() {
            return this.task.getOwningPlugin();
        }

        @Override
        public void cancel() {
            this.task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return this.task.isCancelled();
        }
    }
}
