package org.tbstcraft.quark.internal.task.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitGlobalTaskManager extends BukkitTaskManager {
    BukkitGlobalTaskManager(Plugin owner) {
        super(owner);
    }

    @Override
    protected BukkitTask runInternal(Runnable action) {
        return wrap(action).runTask(this.owner);
    }

    @Override
    protected BukkitTask delayInternal(long delay, Runnable action) {
        return wrap(action).runTaskLater(this.owner, delay);
    }

    @Override
    protected BukkitTask timerInternal(long delay, long period, Runnable action) {
        return wrap(action).runTaskTimer(this.owner, delay, period);
    }
}
