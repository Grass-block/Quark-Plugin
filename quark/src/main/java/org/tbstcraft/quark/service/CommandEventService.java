package org.tbstcraft.quark.service;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.tbstcraft.quark.command.event.CommandEvent;
import org.tbstcraft.quark.command.event.CommandTabEvent;
import org.tbstcraft.quark.util.api.BukkitUtil;

public interface CommandEventService {
    CommandEventAdapter INSTANCE = new CommandEventAdapter();

    static void init() {
        BukkitUtil.registerEventListener(INSTANCE);
    }

    static void stop() {
        BukkitUtil.unregisterEventListener(INSTANCE);
    }

    //well I use this damn method to avoid covering vanilla commands. :D
    final class CommandEventAdapter implements Listener {
        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (exec(event.getPlayer(), event.getMessage().replaceFirst("/", ""))) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onCommand(ServerCommandEvent event) {
            if (exec(event.getSender(), event.getCommand())) {
                event.setCancelled(true);
            }
        }


        @EventHandler
        public void onTabComplete(TabCompleteEvent event) {
            CommandTabEvent evt = new CommandTabEvent(event.getSender(), event.getBuffer(), event.getBuffer().split(" "), event.getCompletions());
            Bukkit.getPluginManager().callEvent(evt);
            if(evt.isCancelled()){
                event.setCancelled(true);
            }
        }

        private boolean exec(CommandSender sender, String commandLine) {
            String[] raw = commandLine.split(" ");
            String[] args = new String[raw.length - 1];
            System.arraycopy(raw, 1, args, 0, raw.length - 1);
            CommandEvent evt = new CommandEvent(sender, raw[0], args);
            Bukkit.getPluginManager().callEvent(evt);
            return evt.isCancelled();
        }
    }
}
