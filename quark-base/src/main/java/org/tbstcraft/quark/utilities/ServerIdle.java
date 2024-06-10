package org.tbstcraft.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.service.base.task.TaskService;

import java.lang.reflect.InvocationTargetException;

@Deprecated
@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class ServerIdle extends PackageModule {
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (Bukkit.getOnlinePlayers().size() == 1) {
            getLogger().info("cancel server stopping process as POWER_SAVING");
            TaskService.cancelTask("quark:ps:countdown");
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (!this.getConfig().getBoolean("instance")) {
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            TaskService.asyncDelayTask("quark:ps:countdown", 20000, this::stop);
        }
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event) {
        this.stop();
    }

    public void stop() {
        getLogger().info("stopping server as POWER_SAVING");
        TaskService.runTask(() -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "stop");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Class.forName("io.papermc.paperclip.Main")
                        .getMethod("main", String[].class)
                        .invoke(null, (Object) new String[0]);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                     ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
