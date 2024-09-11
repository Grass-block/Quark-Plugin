package org.tbstcraft.quark.internal.task.modern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class TaskManagerProvider {
    protected final Plugin owner;

    protected TaskManagerProvider(Plugin owner) {
        this.owner = owner;
    }

    public abstract TaskManager global();

    public abstract TaskManager async();

    public abstract TaskManager entity(Entity entity);

    public abstract TaskManager chunk(World world, int chunkX, int chunkZ);

    public final TaskManager chunk(Location location) {
        return chunk(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public void cleanup(){}
}
