package org.tbstcraft.quark.internal.command;

import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.APIProfileTest;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;
import org.tbstcraft.quark.util.ExceptionUtil;

public interface InternalCommands {
    Listener INVALID_COMMAND_WARN = new InvalidCommandWarn();
    @SuppressWarnings("unchecked")
    AbstractCommand[] COMMANDS = new AbstractCommand[]{new QuarkPluginCommand()};

    static void register() {
        if (APIProfileTest.isFoliaServer()) {
            BukkitUtil.registerEventListener(INVALID_COMMAND_WARN);
        }

        for (AbstractCommand cmd : COMMANDS) {
            try {
                Quark.getInstance().getCommandManager().register(cmd);
            } catch (Exception e) {
                Quark.getInstance()
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
                Quark.getInstance().getCommandManager().unregister(command);
            } catch (Exception e) {
                Quark.getInstance()
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
                Quark.LANGUAGE.sendMessage(event.getSender(), "folia_compat", "reload_warn");
                //event.setCancelled(true);
            }
        }

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getMessage().startsWith("/reload")) {
                Quark.LANGUAGE.sendMessage(event.getPlayer(), "folia_compat", "reload_warn");
                //event.setCancelled(true);
            }
        }
    }
}
