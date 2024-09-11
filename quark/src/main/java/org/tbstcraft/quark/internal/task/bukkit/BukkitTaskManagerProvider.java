package org.tbstcraft.quark.internal.task.bukkit;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.internal.task.modern.TaskManager;
import org.tbstcraft.quark.internal.task.modern.TaskManagerProvider;

public final class BukkitTaskManagerProvider extends TaskManagerProvider {
    private final BukkitAsyncTaskManager async;
    private final BukkitGlobalTaskManager global;

    public BukkitTaskManagerProvider(Plugin owner) {
        super(owner);

        this.async = new BukkitAsyncTaskManager(owner);
        this.global = new BukkitGlobalTaskManager(owner);
    }

    @Override
    public TaskManager global() {
        return this.global;
    }

    @Override
    public TaskManager async() {
        return this.async;
    }

    @Override
    public TaskManager entity(Entity entity) {
        return this.global;
    }

    @Override
    public TaskManager chunk(World world, int chunkX, int chunkZ) {
        return this.global;
    }

    @Override
    public void cleanup() {
        this.async.stop();
        this.global.stop();
    }
}
