package org.tbstcraft.quark;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public interface TaskManager {
    HashMap<String, BukkitTask> TASKS = new HashMap<>();

    static void runTimer(String tid, long delay, long period, Runnable task) {
        TASKS.put(tid, Bukkit.getScheduler().runTaskTimer(Quark.PLUGIN, task, delay, period));
    }

    static void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(Quark.PLUGIN, task);
    }

    static void runLater(long delay, Runnable task) {
        Bukkit.getScheduler().runTaskLater(Quark.PLUGIN, task, delay);
    }

    static void stopAll() {
        for (BukkitTask task : TASKS.values()) {
            task.cancel();
        }
        TASKS.clear();
    }

    static void cancelTask(String tid) {
        BukkitTask task = TASKS.get(tid);

        if (task == null) {
            return;
        }

        task.cancel();
    }

    static BukkitTask getTask(String tid) {
        return TASKS.get(tid);
    }
}
