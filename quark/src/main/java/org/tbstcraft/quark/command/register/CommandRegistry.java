package org.tbstcraft.quark.command.register;

import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.command.event.CommandEvent;
import org.tbstcraft.quark.command.event.CommandTabEvent;
import org.tbstcraft.quark.util.api.BukkitUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public interface CommandRegistry {
    void register(AbstractCommand command);

    void unregister(AbstractCommand command);

    final class DirectCommandRegistry implements CommandRegistry {

        @Override
        public void register(AbstractCommand command) {
            Map<String, Command> commands = CommandManager.getCommandEntries();
            command.fetchCovered();
            commands.put(command.getName(), command);
        }

        @Override
        public void unregister(AbstractCommand command) {
            Map<String, Command> commands = CommandManager.getCommandEntries();
            commands.remove(command.getName());

            Command covered = command.getCovered();
            if (covered == null) {
                return;
            }
            commands.put(command.getName(), covered);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    final class EventCommandRegistry implements CommandRegistry {
        private final Map<String, WrappedCommandExecutor> executorMap = new HashMap<>();

        @Override
        public void register(AbstractCommand command) {
            WrappedCommandExecutor executor = new WrappedCommandExecutor(command);
            BukkitUtil.registerEventListener(executor);
            this.executorMap.put(command.getName(), executor);
        }

        @Override
        public void unregister(AbstractCommand command) {
            WrappedCommandExecutor executor = this.executorMap.get(command.getName());
            if (executor == null) {
                return;
            }
            BukkitUtil.unregisterEventListener(executor);
            this.executorMap.remove(command.getName());
        }


        private static final class WrappedCommandExecutor implements Listener {
            private final AbstractCommand command;

            private WrappedCommandExecutor(AbstractCommand command) {
                this.command = command;
            }

            @EventHandler
            public void onCommand(CommandEvent event) {
                if (!Objects.equals(event.getName(), this.command.getName())) {
                    return;
                }
                event.setCancelled(true);
                this.command.execute(event.getSender(), event.getName(), event.getArgs());
            }

            @EventHandler
            public void onCommandTab(CommandTabEvent event) {
                if (notCurrentCommand(event.getCommandLine())) {
                    return;
                }
                event.setCancelled(true);
                event.getCompletions().clear();
                event.getCompletions().addAll(this.command.tabComplete(event.getSender(), "", event.getArgs()));
            }

            private boolean notCurrentCommand(String line) {
                return !(line.startsWith("/" + this.command.getName()) || line.startsWith(this.command.getName()));
            }
        }
    }
}
