package org.tbstcraft.quark.internal.task;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@QuarkService(id = "task")
public interface TaskService extends Service {
    ServiceHolder<TaskService> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void start() {
        INSTANCE.set(create(Quark.PLUGIN));
        INSTANCE.get().onEnable();
    }

    @ServiceInject
    static void stop() {
        INSTANCE.get().onDisable();
    }

    static void registerTask(String id, Task task) {
        INSTANCE.get().register(id, task);
    }

    static void unregisterTask(String id) {
        INSTANCE.get().unregister(id);
    }

    static Task getTask(String id) {
        return INSTANCE.get().get(id);
    }

    static void cancelTask(String id) {
        INSTANCE.get().cancel(id);
    }

    static String laterTask(long delay, Runnable task) {
        return INSTANCE.get().delay(delay, task);
    }

    static String timerTask(long delay, long period, Runnable task) {
        return INSTANCE.get().timer(delay, period, task);
    }

    static String asyncTask(Runnable task) {
        return INSTANCE.get().async(task);
    }

    static void laterTask(String id, long delay, Runnable task) {
        INSTANCE.get().delay(id, delay, task);
    }

    static void timerTask(String id, long delay, long period, Runnable task) {
        INSTANCE.get().timer(id, delay, period, task);
    }

    static void asyncTask(String id, Runnable task) {
        INSTANCE.get().async(id, task);
    }

    static void asyncDelayTask(String id, long delay, Runnable task) {
        INSTANCE.get().asyncDelay(id, delay, task);
    }

    static void asyncTimerTask(String id, long delay, long period, Runnable task) {
        INSTANCE.get().asyncTimer(id, delay, period, task);
    }

    static void runTask(Runnable task) {
        INSTANCE.get().run(task);
    }

    static void runTask(Location loc, Runnable task) {
        INSTANCE.get().run(loc, task);
    }

    static TaskService create(Plugin plugin) {
        if (APIProfileTest.isFoliaServer()) {
            return new FoliaTaskManager(plugin);
        }
        return new BukkitTaskManager(plugin);
    }

    static Map<String, Task> getAllTasks() {
        return INSTANCE.get().getTasks();
    }

    static void task(Entity target, Runnable task) {
        INSTANCE.get().run(target, task);
    }

    static void delayTask(String id, Entity target, long delay, Runnable task) {
        INSTANCE.get().delay(id, target, delay, task);
    }

    static void timerTask(String id, Entity target, long delay, long period, Runnable task) {
        INSTANCE.get().timer(id, target, delay, period, task);
    }

    static void runDelayTask(Location location, int delay, Runnable task) {
        INSTANCE.get().delay(UUID.randomUUID().toString(), delay, task);
    }

    void register(String id, Task task);

    void unregister(String id);

    Map<String, Task> getTasks();

    Task get(String id);

    default void cancel(String id) {
        Task task = this.get(id);
        if (task == null) {
            return;
        }
        task.cancel();
    }

    void run(Runnable task);

    void delay(String id, long delay, Runnable task);

    void timer(String id, long delay, long period, Runnable task);

    void async(String id, Runnable task);

    void asyncDelay(String id, long delay, Runnable task);

    void asyncTimer(String id, long delay, long period, Runnable task);


    //location
    void run(Location loc, Runnable task);

    void delay(String id, Location loc, long delay, Runnable task);

    void timer(String id, Location loc, long delay, long period, Runnable task);


    //entity
    void run(Entity entity, Runnable task);

    void delay(String id, Entity entity, long delay, Runnable task);

    void timer(String id, Entity entity, long delay, long period, Runnable task);


    default String delay(long delay, Runnable task) {
        String id = UUID.randomUUID().toString();
        delay(id, delay, task);
        return id;
    }

    default String timer(long delay, long period, Runnable task) {
        String id = UUID.randomUUID().toString();
        timer(id, delay, period, task);
        return id;
    }

    default String async(Runnable task) {
        String id = UUID.randomUUID().toString();
        async(id, task);
        return id;
    }

    default String asyncDelay(long delay, Runnable task) {
        String id = UUID.randomUUID().toString();
        asyncDelay(id, delay, task);
        return id;
    }

    default String asyncTimer(long delay, long period, Runnable task) {
        String id = UUID.randomUUID().toString();
        asyncTimer(id, delay, period, task);
        return id;
    }


    interface task {
        void cancel();

        boolean isCancelled();

        Plugin getOwner();
    }
}
