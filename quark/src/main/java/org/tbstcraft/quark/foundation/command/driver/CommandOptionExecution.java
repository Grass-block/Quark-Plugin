package org.tbstcraft.quark.foundation.command.driver;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.foundation.command.CommandExecuter;

import java.lang.reflect.Method;
import java.util.List;

public final class CommandOptionExecution implements CommandExecuter {
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
