package org.tbstcraft.quark.foundation.command.driver;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CommandExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OptionCommandExecutor implements CommandExecutor {
    private final Map<String, Method> handlers = new HashMap<>();
    private final Object executor;

    public OptionCommandExecutor(Object executor) {
        this.executor = executor;

        for (Method method : executor.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(CommandExecutorHandler.class)) {
                return;
            }


        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
    }
}
