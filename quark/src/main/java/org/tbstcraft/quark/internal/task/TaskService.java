package org.tbstcraft.quark.internal.task;

import me.gb2022.commons.container.ObjectContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.internal.task.bukkit.BukkitTaskManager;
import org.tbstcraft.quark.internal.task.bukkit.BukkitTaskManagerProvider;
import org.tbstcraft.quark.internal.task.folia.FoliaTaskManager;
import org.tbstcraft.quark.internal.task.folia.FoliaTaskManagerProvider;
import org.tbstcraft.quark.internal.task.modern.TaskManager;
import org.tbstcraft.quark.internal.task.modern.TaskManagerProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@QuarkService(id = "task")
public interface TaskService extends Service {

    ServiceHolder<TaskService> INSTANCE = new ServiceHolder<>();

    ObjectContainer<TaskManagerProvider> CONTAINER = new ObjectContainer<>();

    Set<Runnable> FINALIZE_TASKS = new HashSet<>();

    @ServiceInject
    static void start() {
        var owner = Quark.getInstance();
        CONTAINER.set(APIProfileTest.folia() ? new FoliaTaskManagerProvider(owner) : new BukkitTaskManagerProvider(owner));
    }

    @ServiceInject
    static void stop() {
        CONTAINER.get().cleanup();
    }

    static TaskManager global() {
        return CONTAINER.get().global();
    }

    static TaskManager async() {
        return CONTAINER.get().async();
    }

    static TaskManager region(Location loc) {
        return CONTAINER.get().chunk(loc);
    }

    static TaskManager region(World world, int cx, int cz) {
        return CONTAINER.get().chunk(world, cx, cz);
    }

    static TaskManager entity(Entity entity) {
        return CONTAINER.get().entity(entity);
    }

    static void registerFinalizeTask(Runnable command) {
        FINALIZE_TASKS.add(command);
    }

    static void runFinalizeTask() {
        for (Runnable task : FINALIZE_TASKS) {
            task.run();
        }
        FINALIZE_TASKS.clear();
    }
}
