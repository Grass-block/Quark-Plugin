package org.tbstcraft.quark.internal.task.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.internal.task.Task;
import org.tbstcraft.quark.internal.task.modern.AutoCancelledTaskWrapper;
import org.tbstcraft.quark.internal.task.modern.TaskManager;

import java.util.function.Consumer;

public abstract class FoliaTaskManager extends TaskManager {
    protected FoliaTaskManager(Plugin owner) {
        super(owner);
    }

    @Override
    public Task run(String id, Consumer<Task> action) {
        var wrapper = new ThreadedRegionTaskWrapper();
        var command = new AutoCancelledTaskWrapper(action, this);

        register(id, wrapper);
        wrapper.setHandle(this.runInternal((handle) -> command.accept(wrapper)));

        return wrapper;
    }

    @Override
    public Task delay(String id, long delay, Consumer<Task> action) {
        var wrapper = new ThreadedRegionTaskWrapper();
        var command = new AutoCancelledTaskWrapper(action, this);

        register(id, wrapper);
        wrapper.setHandle(this.delayInternal(delay, (handle) -> command.accept(wrapper)));

        return wrapper;
    }

    @Override
    public Task timer(String id, long delay, long period, Consumer<Task> action) {
        var wrapper = new ThreadedRegionTaskWrapper();

        register(id, wrapper);
        wrapper.setHandle(this.timerInternal(delay, period, (handle) -> action.accept(wrapper)));

        return wrapper;
    }

    protected abstract ScheduledTask runInternal(Consumer<ScheduledTask> action);

    protected abstract ScheduledTask delayInternal(long delay, Consumer<ScheduledTask> action);

    protected abstract ScheduledTask timerInternal(long delay, long period, Consumer<ScheduledTask> action);
}
