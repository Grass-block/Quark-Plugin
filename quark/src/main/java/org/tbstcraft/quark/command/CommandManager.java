package org.tbstcraft.quark.command;

import org.tbstcraft.quark.util.BukkitUtil;
import org.bukkit.command.Command;
import org.bukkit.command.*;

import java.lang.reflect.Field;
import java.util.HashMap;

public interface CommandManager {
    static void registerCommand(Command command) {
        CommandMap map = BukkitUtil.getAndFuckCommandMap();
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);//让我访问!!
            Object o = field.get(map);
            ((HashMap) o).put(command.getName(), command);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static void unregisterCommand(Command command) {
        CommandMap map = BukkitUtil.getAndFuckCommandMap();
        try {
            Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
            field.setAccessible(true);//让我访问!!
            Object o = field.get(map);
            ((HashMap) o).remove(command.getName());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
