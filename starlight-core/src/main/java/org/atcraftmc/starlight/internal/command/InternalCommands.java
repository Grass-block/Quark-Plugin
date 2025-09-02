package org.atcraftmc.starlight.internal.command;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.Starlight;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.atcraftmc.starlight.foundation.platform.APIProfileTest;
import org.atcraftmc.starlight.foundation.platform.BukkitUtil;
import org.atcraftmc.starlight.util.ExceptionUtil;

public interface InternalCommands {
    Listener INVALID_COMMAND_WARN = new InvalidCommandWarn();
    AbstractCommand[] COMMANDS = new AbstractCommand[]{new StarlightPluginCommand()};

    static void register() {
        if (APIProfileTest.isFoliaServer()) {
            BukkitUtil.registerEventListener(INVALID_COMMAND_WARN);
        }

        for (AbstractCommand cmd : COMMANDS) {
            try {
                Starlight.instance().getCommandManager().register(cmd);
            } catch (Exception e) {
                Starlight.instance()
                        .getLogger()
                        .severe("failed to register internal command %s: %s".formatted(cmd.getClass()
                                                                                               .getAnnotation(QuarkCommand.class)
                                                                                               .name(), ExceptionUtil.getMessage(e)));
            }
        }

    }

    static void unregister() {
        for (AbstractCommand command : COMMANDS) {
            try {
                Starlight.instance().getCommandManager().unregister(command);
            } catch (Exception e) {
                Starlight.instance()
                        .getLogger()
                        .severe("failed to unregister internal command %s: %s".formatted(command.getClass()
                                                                                                 .getAnnotation(QuarkCommand.class)
                                                                                                 .name(), ExceptionUtil.getMessage(e)));
            }
        }
    }


    final class InvalidCommandWarn implements Listener {
        @EventHandler
        public void onCommand(ServerCommandEvent event) {
            if (event.getCommand().startsWith("reload")) {
                Starlight.LANGUAGE.item("folia-compat:reload-warn").send(event.getSender());
                //event.setCancelled(true);
            }
        }

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getMessage().startsWith("/reload")) {
                Starlight.LANGUAGE.item("folia-compat:reload-warn").send(event.getPlayer());
                //event.setCancelled(true);
            }
        }
    }
}
