package org.tbstcraft.quark.internal.task;

import me.gb2022.commons.container.ObjectContainer;
import org.atcraftmc.qlib.task.TaskManager;
import org.atcraftmc.qlib.task.TaskScheduler;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceInject;


@QuarkService(id = "task")
public interface TaskService extends Service {
    ObjectContainer<TaskManager> CONTAINER = new ObjectContainer<>();

    @ServiceInject
    static void start() {
        CONTAINER.set(TaskManager.getInstance(Quark.getInstance()));
    }

    @ServiceInject
    static void stop() {
        CONTAINER.get().cleanup();
    }

    static TaskScheduler global() {
        return CONTAINER.get().global();
    }

    static TaskScheduler async() {
        return CONTAINER.get().async();
    }

    static TaskScheduler region(Location loc) {
        return CONTAINER.get().chunk(loc);
    }

    static TaskScheduler region(World world, int cx, int cz) {
        return CONTAINER.get().chunk(world, cx, cz);
    }

    static TaskScheduler entity(Entity entity) {
        return CONTAINER.get().entity(entity);
    }

    static void registerFinalizeTask(Runnable command) {
        CONTAINER.get().registerFinalizeTask(command);
    }

    static void runFinalizeTask() {
        CONTAINER.get().runFinalizeTask();
    }
}
