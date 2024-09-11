package org.tbstcraft.quark.internal.task.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.tbstcraft.quark.internal.task.Task;
import org.tbstcraft.quark.internal.task.modern.AutoCancelledTaskWrapper;
import org.tbstcraft.quark.internal.task.modern.TaskManager;

import java.util.function.Consumer;

public abstract class BukkitTaskManager extends TaskManager {
    protected BukkitTaskManager(Plugin owner) {
        super(owner);
    }

    static BukkitRunnable wrap(Runnable command) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                command.run();
            }
        };
    }

    @Override
    public Task run(String id, Consumer<Task> action) {
        var wrapper = new BukkitTaskWrapper();
        var command = new AutoCancelledTaskWrapper(action, this);

        register(id, wrapper);
        wrapper.setHandle(this.runInternal(() -> command.accept(wrapper)));

        return wrapper;
    }

    @Override
    public Task delay(String id, long delay, Consumer<Task> action) {
        var wrapper = new BukkitTaskWrapper();
        var command = new AutoCancelledTaskWrapper(action, this);

        register(id, wrapper);
        wrapper.setHandle(this.delayInternal(delay, () -> command.accept(wrapper)));

        return wrapper;
    }

    @Override
    public Task timer(String id, long delay, long period, Consumer<Task> action) {
        var wrapper = new BukkitTaskWrapper();

        register(id, wrapper);
        wrapper.setHandle(this.timerInternal(delay, period, () -> action.accept(wrapper)));

        return wrapper;
    }

    protected abstract BukkitTask runInternal(Runnable action);

    protected abstract BukkitTask delayInternal(long delay, Runnable action);

    protected abstract BukkitTask timerInternal(long delay, long period, Runnable action);
}
