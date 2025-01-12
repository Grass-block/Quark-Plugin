package org.atcraftmc.quark_velocity.util;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.InvocableCommand;
import me.gb2022.commons.reflect.Annotations;
import org.atcraftmc.quark_velocity.QuarkVelocity;

public final class VelocityCommandManager {
    private final QuarkVelocity plugin;

    public VelocityCommandManager(QuarkVelocity plugin) {
        this.plugin = plugin;
    }

    public CommandManager getCommandManager() {
        return this.plugin.getServer().getCommandManager();
    }

    public void registerCommand(InvocableCommand<?> command, String name, String... aliases) {
        CommandManager commandManager = getCommandManager();
        CommandMeta meta = commandManager.metaBuilder(name).aliases(aliases).plugin(this.plugin).build();

        commandManager.register(meta, command);
    }

    public void unregisterCommand(String name, String... aliases) {
        CommandManager commandManager = getCommandManager();
        CommandMeta meta = commandManager.metaBuilder(name).aliases(aliases).plugin(this.plugin).build();

        commandManager.unregister(meta);
    }

    public void registerCommand(InvocableCommand<?> command) {
        Annotations.matchAnnotation(command, VelocityCommand.class, (c) -> registerCommand(command, c.name(), c.aliases()));
    }

    public void unregisterCommand(InvocableCommand<?> command) {
        Annotations.matchAnnotation(command, VelocityCommand.class, (c) -> unregisterCommand(c.name(), c.aliases()));
    }
}
