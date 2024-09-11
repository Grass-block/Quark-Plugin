package org.tbstcraft.quark.foundation.platform;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;

import java.lang.reflect.InvocationTargetException;

public interface FoliaServer {

    static RegionScheduler getRegionScheduler() {
        validateFoliaServer();
        Server server = Bukkit.getServer();
        try {
            return (RegionScheduler) server.getClass().getMethod("getRegionScheduler").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static AsyncScheduler getAsyncScheduler() {
        validateFoliaServer();
        Server server = Bukkit.getServer();
        try {
            return (AsyncScheduler) server.getClass().getMethod("getAsyncScheduler").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static EntityScheduler getEntityScheduler(Entity entity) {
        validateFoliaServer();
        try {
            return (EntityScheduler) entity.getClass().getMethod("getScheduler").invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void validateFoliaServer() {
        if (APIProfileTest.isFoliaServer()) {
            return;
        }
        throw new RuntimeException("NOT a Folia Server!");
    }

    static GlobalRegionScheduler getGlobalScheduler() {
        validateFoliaServer();
        Server server = Bukkit.getServer();
        try {
            return (GlobalRegionScheduler) server.getClass().getMethod("getGlobalRegionScheduler").invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
