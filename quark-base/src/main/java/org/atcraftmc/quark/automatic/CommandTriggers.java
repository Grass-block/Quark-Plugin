package org.atcraftmc.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.placeholder.PlaceHolderService;
import org.atcraftmc.starlight.core.TaskService;

/**
 * <code:yml></code>
 */
@SLModule(version = "1.0.0")
public class CommandTriggers extends PackageModule {

    public static void runCommand(String command, Player player) {
        TaskService.global().run(() -> {
            var line = PlaceHolderService.format(command);
            line = PlaceHolderService.formatPlayer(player, line);

            Bukkit.dispatchCommand(player, line);
        });
    }

    private void callEvent(Player holder,String event){

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        callEvent(event.getPlayer(),"player_join");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        callEvent(event.getPlayer(),"player_quit");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        callEvent(event.getPlayer(),"player_respawn");
    }


}

// #[listener]player_join,player_quit
// give {player} item 1 {meta}
// self-msg {#blue}aaaaaaa
// @console mv tp {player} world
// #[end]

