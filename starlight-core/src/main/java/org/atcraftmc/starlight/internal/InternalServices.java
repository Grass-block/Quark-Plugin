package org.atcraftmc.starlight.internal;

import org.atcraftmc.starlight.Starlight;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.api.event.CommandEvent;
import org.atcraftmc.starlight.api.event.CommandTabEvent;
import me.gb2022.modular.service.ApplicationService;
import org.atcraftmc.starlight.framework.SLService;
import me.gb2022.modular.service.ServiceHolder;
import me.gb2022.modular.service.injection.ServiceInject;
import org.atcraftmc.starlight.internal.command.InternalCommands;

public interface InternalServices {
    @ApplicationService(id = "internal#bungee-channel-supplier")
    interface BungeeChannelSupplier extends SLService {
        String BUNGEE_CHANNEL_ID = "BungeeCord";

        @ServiceInject
        static void start() {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Starlight.instance(), BUNGEE_CHANNEL_ID);
        }

        @ServiceInject
        static void stop() {
            Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(Starlight.instance(), BUNGEE_CHANNEL_ID);
        }
    }

    @ApplicationService(id = "internal#command-provider")
    interface InternalCommandsProvider extends SLService {
        @ServiceInject
        static void start() {
            InternalCommands.register();
        }

        @ServiceInject
        static void stop() {
            InternalCommands.unregister();
        }
    }

    @ApplicationService(id = "internal#command-event", impl = CommandEventService.CommandEventAdapter.class)
    interface CommandEventService extends SLService {

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
                CommandTabEvent evt = new CommandTabEvent(
                        event.getSender(),
                        event.getBuffer(),
                        event.getBuffer().split(" "),
                        event.getCompletions()
                );
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
}
