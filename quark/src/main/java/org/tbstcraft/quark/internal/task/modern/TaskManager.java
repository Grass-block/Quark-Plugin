package org.tbstcraft.quark.internal.task.modern;

import me.gb2022.commons.container.MultiMap;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.internal.task.Task;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class TaskManager {
    protected final Plugin owner;
    private final MultiMap<String, Task> tasks = new MultiMap<>();

    protected TaskManager(Plugin owner) {
        this.owner = owner;
    }

    public abstract Task run(String id, Consumer<Task> handler);

    public abstract Task delay(String id, long ticks, Consumer<Task> handler);

    public abstract Task timer(String id, long delay, long period, Consumer<Task> handler);


    //alias
    public final Task run(Runnable handler) {
        return this.run(UUID.randomUUID().toString(), (c) -> handler.run());
    }

    public final Task delay(long ticks, Runnable handler) {
        return this.delay(UUID.randomUUID().toString(), ticks, (c) -> handler.run());
    }

    public final Task timer(long delay, long period, Runnable handler) {
        return this.timer(UUID.randomUUID().toString(), delay, period, (c) -> handler.run());
    }

    public final Task run(String id, Runnable handler) {
        return this.run(id, (c) -> handler.run());
    }

    public final Task delay(String id, long ticks, Runnable handler) {
        return this.delay(id, ticks, (c) -> handler.run());
    }

    public final Task timer(String id, long delay, long period, Runnable handler) {
        return this.timer(id, delay, period, (c) -> handler.run());
    }

    public final Task run(Consumer<Task> handler) {
        return this.run(UUID.randomUUID().toString(), handler);
    }

    public final Task delay(long ticks, Consumer<Task> handler) {
        return this.delay(UUID.randomUUID().toString(), ticks, handler);
    }

    public final Task timer(long delay, long period, Consumer<Task> handler) {
        return this.timer(UUID.randomUUID().toString(), delay, period, handler);
    }

    public void stop() {
        for (String key : new HashSet<>(this.tasks.keySet())) {
            this.cancel(key);
        }

        this.cleanup(this.owner);
    }

    public void cleanup(Plugin owner) {

    }

    protected void register(String id, Task task) {
        this.tasks.put(id, task);
    }

    public Task get(String id) {
        return this.tasks.get(id);
    }

    public void cancel(Task task) {
        if (!task.isCancelled()) {
            task.cancel();
        }

        this.tasks.remove(this.tasks.of(task));
    }

    public void cancel(String id) {
        var task = this.tasks.get(id);
        if (task == null) {
            return;
        }

        if (!task.isCancelled()) {
            task.cancel();
        }

        this.tasks.remove(id);
    }

    public Set<String> tasks() {
        return this.tasks.keySet();
    }
}
