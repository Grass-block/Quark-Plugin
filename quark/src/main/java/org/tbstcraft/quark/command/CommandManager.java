package org.tbstcraft.quark.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.command.register.CommandRegistry;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface CommandManager {
    CommandRegistry.DirectCommandRegistry DIRECT = new CommandRegistry.DirectCommandRegistry();
    CommandRegistry.EventCommandRegistry EVENT = new CommandRegistry.EventCommandRegistry();

    @SuppressWarnings("unchecked")
    static void registerCommand(Command command) {
        if (command instanceof AbstractCommand cmd) {
            registerQuarkCommand(cmd);
            return;
        }
        String id = command.getName();
        try {
            CommandMap map = getCommandMap();
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            Object o = field.get(map);
            ((HashMap<String, Command>) o).put(id, command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ExceptionUtil.log(Quark.LOGGER, e);
        }
    }

    static void unregisterCommand(Command command) {
        if (command instanceof AbstractCommand cmd) {
            unregisterQuarkCommand(cmd);
            return;
        }
        unregisterCommand(command.getName());
    }

    static void unregisterCommand(String name) {
        CommandMap map = getCommandMap();
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);
            Object o = field.get(map);
            ((HashMap<?, ?>) o).remove(name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ExceptionUtil.log(Quark.LOGGER, e);
        }
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

    static void syncCommands() {
        try {
            Server server = Bukkit.getServer();
            server.getClass().getDeclaredMethod("syncCommands").invoke(server);
        } catch (Exception e) {
            ExceptionUtil.log(e);
        }
    }

    static void registerQuarkCommand(AbstractCommand command) {
        if (command.isEventBased()) {
            EVENT.register(command);
        } else {
            DIRECT.register(command);
        }
    }

    static void unregisterQuarkCommand(AbstractCommand command) {
        if (command.isEventBased()) {
            EVENT.unregister(command);
        } else {
            DIRECT.unregister(command);
        }
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
}

