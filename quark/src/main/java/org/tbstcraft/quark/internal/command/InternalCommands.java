package org.tbstcraft.quark.internal.command;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.command.AbstractCommand;
import org.tbstcraft.quark.framework.command.CommandManager;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.util.platform.APIProfileTest;
import org.tbstcraft.quark.util.platform.BukkitUtil;
import org.tbstcraft.quark.util.ExceptionUtil;

public interface InternalCommands {
    Listener INVALID_COMMAND_WARN = new InvalidCommandWarn();
    @SuppressWarnings("unchecked")
    Class<? extends AbstractCommand>[] COMMANDS = new Class[]{
            SetPasswordCommand.class,
            QuarkPluginCommand.class
    };

    static void register() {
        if (APIProfileTest.isFoliaServer()) {
            BukkitUtil.registerEventListener(INVALID_COMMAND_WARN);
        }
        for (Class<? extends AbstractCommand> clazz : COMMANDS) {
            try {
                CommandManager.registerCommand(clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                Quark.LOGGER.severe("failed to register internal command %s: %s".formatted(
                        clazz.getAnnotation(QuarkCommand.class).name(),
                        ExceptionUtil.getMessage(e)
                ));
            }
        }
    }

    static void unregister() {
        for (Class<? extends AbstractCommand> clazz : COMMANDS) {
            try {
                CommandManager.unregisterCommand(clazz.getAnnotation(QuarkCommand.class).name());
            } catch (Exception e) {
                Quark.LOGGER.severe("failed to unregister internal command %s: %s".formatted(
                        clazz.getAnnotation(QuarkCommand.class).name(),
                        ExceptionUtil.getMessage(e)
                ));
            }
        }
    }


    final class InvalidCommandWarn implements Listener {
        @EventHandler
        public void onCommand(ServerCommandEvent event) {
            if (event.getCommand().startsWith("reload")) {
                Quark.LANGUAGE.sendMessage(event.getSender(), "folia_compat", "reload_warn");
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onCommand(PlayerCommandPreprocessEvent event) {
            if (event.getMessage().startsWith("/reload")) {
                Quark.LANGUAGE.sendMessage(event.getPlayer(), "folia_compat", "reload_warn");
                event.setCancelled(true);
            }
        }
    }
}
