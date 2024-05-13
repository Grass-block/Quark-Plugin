package org.tbstcraft.quark.internal;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.tbstcraft.quark.framework.event.command.CommandEvent;
import org.tbstcraft.quark.framework.event.command.CommandTabEvent;
import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;
import org.tbstcraft.quark.util.api.BukkitUtil;

@QuarkService(id = "command-event", impl = CommandEventService.CommandEventAdapter.class)
public interface CommandEventService extends Service {

    @ServiceInject
    ServiceHolder<CommandEventAdapter> INSTANCE = new ServiceHolder<>();

    @ServiceInject
    static void init() {
        BukkitUtil.registerEventListener(INSTANCE.get());
    }

    @ServiceInject
    static void stop() {
        BukkitUtil.unregisterEventListener(INSTANCE.get());
    }

    //well I use this damn method to avoid covering vanilla commands. :D
    final class CommandEventAdapter implements Listener, CommandEventService {
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
            if (evt.isCancelled()) {
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
