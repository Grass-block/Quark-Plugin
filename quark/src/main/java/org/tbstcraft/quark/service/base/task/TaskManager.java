package org.tbstcraft.quark.service.base.task;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public abstract class TaskManager implements TaskService {
    public static final Location DEFAULT = new Location(Bukkit.getWorld("world"), 0, 0, 0);
    private final Map<String, Task> tasks = new HashMap<>();

    private final Plugin plugin;

    public TaskManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public final void run(Runnable task) {
        this.run(DEFAULT, task);
    }

    public final void delay(String id, long delay, Runnable task) {
        this.delay(id, DEFAULT, delay, task);
    }

    public final void timer(String id, long delay, long period, Runnable task) {
        this.timer(id, DEFAULT, delay, period, task);
    }

    @Override
    public void unregister(String id) {
        this.tasks.remove(id);
    }

    @Override
    public void register(String id, Task task) {
        this.tasks.put(id, task);
    }

    @Override
    public Map<String, Task> getTasks() {
        return this.tasks;
    }

    @Override
    public Task get(String id) {
        return this.tasks.get(id);
    }
}
