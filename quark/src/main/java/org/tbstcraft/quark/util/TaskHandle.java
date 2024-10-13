package org.tbstcraft.quark.util;

import org.atcraftmc.qlib.task.Task;
import org.atcraftmc.qlib.task.TaskScheduler;

import java.util.function.Consumer;

public final class TaskHandle {
    public static final int DIRECT = 0;
    public static final int DELAY = 1;
    public static final int TIMER = 2;

    private final TaskScheduler scheduler;
    private final String tid;

    private final int mode;
    private final int delay;
    private final int period;

    private final Consumer<Task> handle;

    public TaskHandle(TaskScheduler scheduler, String tid, int mode, int delay, int period, Consumer<Task> handle) {
        this.scheduler = scheduler;
        this.tid = tid;
        this.mode = mode;
        this.delay = delay;
        this.period = period;
        this.handle = handle;
    }

    public static TaskHandle direct(TaskScheduler scheduler, String tid, Consumer<Task> handle) {
        return new TaskHandle(scheduler, tid, DIRECT, 0, 0, handle);
    }

    public static TaskHandle delay(TaskScheduler scheduler, String tid, int delay, Consumer<Task> handle) {
        return new TaskHandle(scheduler, tid, DELAY, delay, 0, handle);
    }

    public static TaskHandle timer(TaskScheduler scheduler, String tid, int delay, int period, Consumer<Task> handle) {
        return new TaskHandle(scheduler, tid, TIMER, delay, period, handle);
    }

    public static TaskHandle direct(TaskScheduler scheduler, String tid, Runnable handle) {
        return direct(scheduler, tid, (c) -> handle.run());
    }

    public static TaskHandle delay(TaskScheduler scheduler, String tid, int delay, Runnable handle) {
        return delay(scheduler, tid, delay, (c) -> handle.run());
    }

    public static TaskHandle timer(TaskScheduler scheduler, String tid, int delay, int period, Runnable handle) {
        return timer(scheduler, tid, delay, period, (c) -> handle.run());
    }

    public void start() {
        switch (this.mode) {
            case DIRECT -> this.scheduler.run(this.tid, this.handle);
            case DELAY -> this.scheduler.delay(this.tid, this.delay, this.handle);
            case TIMER -> this.scheduler.timer(this.tid, this.delay, this.period, this.handle);
        }
    }

    public void stop() {
        this.scheduler.cancel(this.tid);
    }
}
