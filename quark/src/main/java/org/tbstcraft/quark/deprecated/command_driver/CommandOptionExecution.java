package org.tbstcraft.quark.deprecated.command_driver;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.execute.CommandExecutor;

import java.lang.reflect.Method;

public final class CommandOptionExecution implements CommandExecutor {
    private final Method method;
    private final CommandExecutorHandler descriptor;

    public CommandOptionExecution(Method method) {
        this.method = method;
        this.descriptor = method.getAnnotation(CommandExecutorHandler.class);
    }

    public CommandExecutorHandler getDescriptor() {
        return descriptor;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }
}
