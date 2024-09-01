package org.tbstcraft.quark.internal.task;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.tbstcraft.quark.SharedObjects;

import java.util.UUID;

public final class BukkitTaskManager extends TaskManager {

    public BukkitTaskManager(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void run(Location loc, Runnable task) {
        try {
            BukkitTaskWrapper.once(UUID.randomUUID().toString(), this, task).runTask(this.getPlugin());
        } catch (IllegalPluginAccessException e) {
            TaskService.registerFinalizeTask(task);
        }
    }

    @Override
    public void delay(String id, Location loc, long delay, Runnable task) {
        BukkitTaskWrapper.once(id, this, task).runTaskLater(this.getPlugin(), delay);
    }

    @Override
    public void timer(String id, Location loc, long delay, long period, Runnable task) {
        BukkitTaskWrapper.wrap(id, this, task).runTaskTimer(this.getPlugin(), delay, period);
    }

    @Override
    public void async(String id, Runnable task) {
        try {
            BukkitTaskWrapper.once(id, this, task).runTaskAsynchronously(this.getPlugin());
        } catch (IllegalPluginAccessException e) {
            SharedObjects.SHARED_THREAD_POOL.submit(task);
        }
    }

    @Override
    public void asyncDelay(String id, long delay, Runnable task) {
        BukkitTaskWrapper.once(id, this, task).runTaskLaterAsynchronously(this.getPlugin(), delay);
    }

    @Override
    public void asyncTimer(String id, long delay, long period, Runnable task) {
        BukkitTaskWrapper.wrap(id, this, task).runTaskTimerAsynchronously(this.getPlugin(), delay, period);
    }

    @Override
    public void cancel(String id) {
        super.cancel(id);
    }

    @Override
    public void run(Entity entity, Runnable task) {
        BukkitTaskWrapper.once(UUID.randomUUID().toString(), this, task).runTask(this.getPlugin());
    }

    @Override
    public void delay(String id, Entity entity, long delay, Runnable task) {
        BukkitTaskWrapper.once(id, this, task).runTaskLater(this.getPlugin(), delay);
    }

    @Override
    public void timer(String id, Entity entity, long delay, long period, Runnable task) {
        BukkitTaskWrapper.wrap(id, this, task).runTaskTimer(this.getPlugin(), delay, period);
    }

    public abstract static class BukkitTaskWrapper extends BukkitRunnable implements Task {
        private final String id;
        private final Runnable task;
        private final BukkitTaskManager parent;

        private BukkitTaskWrapper(String id, Runnable task, BukkitTaskManager parent) {
            this.id = id;
            this.task = task;
            this.parent = parent;
            this.parent.register(id, this);
        }

        public static BukkitRunnable wrap(String id, BukkitTaskManager parent, Runnable task) {
            return new SimpleWrapper(id, task, parent);
        }

        public static BukkitRunnable once(String id, BukkitTaskManager parent, Runnable task) {
            return new AutoCancelWrapper(id, task, parent);
        }

        @Override
        public void run() {
            this.task.run();
        }

        public BukkitTaskManager getParent() {
            return parent;
        }

        public String getId() {
            return id;
        }

        public Runnable getTask() {
            return task;
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            this.parent.unregister(this.id);
        }

        @Override
        public Plugin getOwner() {
            return this.parent.getPlugin();
        }

        private static final class SimpleWrapper extends BukkitTaskWrapper {
            private SimpleWrapper(String id, Runnable task, BukkitTaskManager parent) {
                super(id, task, parent);
            }
        }

        private static final class AutoCancelWrapper extends BukkitTaskWrapper {
            private AutoCancelWrapper(String id, Runnable task, BukkitTaskManager parent) {
                super(id, task, parent);
            }

            @Override
            public void run() {
                super.run();
                this.getParent().unregister(this.getId());
            }
        }
    }
}
