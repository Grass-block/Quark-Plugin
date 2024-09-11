package org.tbstcraft.quark.internal.task.folia;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.internal.task.modern.TaskManager;
import org.tbstcraft.quark.internal.task.modern.TaskManagerProvider;

import java.util.HashMap;
import java.util.Map;

public final class FoliaTaskManagerProvider extends TaskManagerProvider {
    private final Map<String, FoliaRegionTaskManager> regions = new HashMap<>();
    private final Map<Entity, FoliaEntityTaskManager> entities = new HashMap<>();

    private final FoliaAsyncTaskManager async;
    private final FoliaGlobalTaskManager global;


    public FoliaTaskManagerProvider(Plugin owner) {
        super(owner);

        this.async = new FoliaAsyncTaskManager(owner);
        this.global = new FoliaGlobalTaskManager(owner);
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
        return this.entities.computeIfAbsent(entity, (e) -> new FoliaEntityTaskManager(this.owner, e));
    }

    @Override
    public TaskManager chunk(World world, int chunkX, int chunkZ) {
        var hash = world.getName() + "@" + chunkX + "," + chunkZ;

        return this.regions.computeIfAbsent(hash, (k) -> new FoliaRegionTaskManager(this.owner, world, chunkX, chunkZ));
    }

    @Override
    public void cleanup() {
        for (Map.Entry<String, FoliaRegionTaskManager> entry : this.regions.entrySet()) {
            entry.getValue().stop();
        }
        this.regions.clear();

        for (Map.Entry<Entity, FoliaEntityTaskManager> entry : this.entities.entrySet()) {
            entry.getValue().stop();
        }
        this.entities.clear();

        this.async.stop();
        this.global.stop();
    }
}
