package org.tbstcraft.quark.internal.task.modern;

import org.tbstcraft.quark.internal.task.Task;

import java.util.function.Consumer;

public final class AutoCancelledTaskWrapper implements Consumer<Task> {
    private final Consumer<Task> action;
    private final TaskManager owner;

    public AutoCancelledTaskWrapper(Consumer<Task> action, TaskManager owner) {
        this.action = action;
        this.owner = owner;
    }

    @Override
    public void accept(Task task) {
        this.action.accept(task);
        this.owner.cancel(task);
    }
}
