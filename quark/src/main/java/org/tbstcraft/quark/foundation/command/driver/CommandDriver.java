package org.tbstcraft.quark.foundation.command.driver;

import me.gb2022.commons.reflect.Annotations;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CommandSyntaxException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class CommandDriver {

    private final Map<String, CommandExecutorInstance> executors = new HashMap<>();

    public static boolean checkHandlerAccess(Method method, Object executor) {
        if (Modifier.isStatic(method.getModifiers())) {
            return executor == null;
        } else {
            return executor != null;
        }
    }

    public void registerHandler(Method method, Object executor) {
        if (!checkHandlerAccess(method, executor)) {
            return;
        }
        if (!method.isAnnotationPresent(CommandExecutor.class)) {
            return;
        }
    }

    public void registerCommandExecutor(Object executor) {
        for (Method method : executor.getClass().getDeclaredMethods()) {
            Annotations.matchAnnotation(method, CommandExecutor.class, (e) -> {
                if (Modifier.isStatic(method.getModifiers())) {
                    return;
                }
            });

            Annotations.matchAnnotation(method, CommandExecutor.class, (e) -> {
                if (Modifier.isStatic(method.getModifiers())) {
                    return;
                }
            });
        }
    }

    public void unregisterCommandExecutor(Object executor) {

    }

    public void registerCommandExecutor(Class<?> executor) {

    }

    public void unregisterCommandExecutor(Class<?> executor) {
    }


    public void execute(CommandSender sender, String command, String[] args) {
        for (CommandExecutorInstance instance : executors.values()) {
            int i = instance.match(command, args);

            if (i == -1) {
                continue;
            }

            String[] offset = new String[i];
            System.arraycopy(args, 0, offset, 0, i);
            try {
                instance.execute(sender, offset);
            } catch (CommandSyntaxException e) {
                //syntax exception
            } catch (Throwable t) {
                //internal exception
            }
            return;
        }
    }

}
