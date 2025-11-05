package org.atcraftmc.starlight.core;

import me.gb2022.commons.container.ObjectContainer;
import org.atcraftmc.qlib.bukkit.task.TaskManager;
import org.atcraftmc.qlib.bukkit.task.TaskScheduler;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.SLService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.injection.ServiceInject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;


@ApplicationService(id = "task")
public interface TaskService extends SLService {
    ObjectContainer<TaskManager> CONTAINER = new ObjectContainer<>();

    @ServiceInject
    static void start() {
        CONTAINER.set(TaskManager.getInstance(Starlight.instance()));
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

    static <I> void future(Future<I> future, Consumer<I> consumer) {
        async().run(()-> {
            try {
                consumer.accept(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
