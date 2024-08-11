package org.tbstcraft.quark.foundation.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.foundation.platform.BukkitUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("unused")
public interface CommandManager {
    CommandRegistry.DirectCommandRegistry DIRECT = new CommandRegistry.DirectCommandRegistry();
    CommandRegistry.EventCommandRegistry EVENT = new CommandRegistry.EventCommandRegistry();

    @SuppressWarnings("rawtypes")
    static Map<String, Command> getKnownCommands(CommandMap map) {
        try {
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Object cmdMap = f.get(map);
            return (Map) cmdMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static CommandMap getCommandMap() {
        Class<?> c = Bukkit.getServer().getClass();
        try {
            Method m = c.getMethod("getCommandMap");
            m.setAccessible(true);
            return (CommandMap) m.invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void register(Command command) {
        CommandMap map = getCommandMap();
        Map<String, Command> knownCommands = getKnownCommands(map);

        command.register(map);

        if (command instanceof AbstractCommand cmd) {
            cmd.fetchCovered();
            cmd.init();
            BukkitUtil.registerEventListener(cmd);

            knownCommands.put("quark:" + command.getName(), cmd);
        }

        knownCommands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            knownCommands.put(alias, command);
        }
    }

    static Command unregister(String name) {
        CommandMap map = getCommandMap();
        Map<String, Command> knownCommands = getKnownCommands(map);

        Command c = knownCommands.get(name);

        knownCommands.remove(name);

        if (c == null) {
            return null;
        }
        c.unregister(map);
        for (String alias : c.getAliases()) {
            knownCommands.remove(alias);
        }
        if (c instanceof AbstractCommand cmd) {
            knownCommands.remove("quark:" + name);

            BukkitUtil.unregisterEventListener(cmd);
            Command covered = cmd.getCovered();
            if (covered == null) {
                return c;
            }
            knownCommands.put(name, covered);
        }
        return c;
    }

    static void unregister(Command command) {
        unregister(command.getName());
    }

    static boolean isQuarkCommand(String id) {
        return getCommandEntries().get(id) instanceof AbstractCommand;
    }

    @SuppressWarnings({"unchecked", "RedundantClassCall"})
    static Collection<String> getCommands() {
        CommandMap map = getCommandMap();
        try {
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            Object cmdMap = f.get(map);
            return Map.class.cast(cmdMap).keySet();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    static void sync() {
        try {
            Server server = Bukkit.getServer();
            server.getClass().getDeclaredMethod("syncCommands").invoke(server);
        } catch (Exception e) {
            Quark.LOGGER.warning("cannot sync commands." + e.getMessage());
        }
    }

    static void registerQuarkCommand(AbstractCommand command) {
        register(command);
    }

    static void unregisterQuarkCommand(AbstractCommand command) {
        unregister(command);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Command> getCommandEntries() {
        try {
            CommandMap map = CommandManager.getCommandMap();
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            Object o = field.get(map);
            return (Map<String, Command>) o;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static AbstractCommand getQuarkCommand(String name) {
        return (AbstractCommand) getCommandEntries().get(name);
    }
}

