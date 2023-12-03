package org.tbstcraft.quark.module;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.util.BukkitUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public abstract class CommandModule extends PluginModule {
    final Command coverageCommand = getCoveredCommand();
    private final AbstractCommand commandAdapter = new AdapterCommand<>(this);

    @Override
    public void onEnable() {
        CommandManager.registerCommand(this.commandAdapter);
    }

    @Override
    public void onDisable() {
        CommandManager.unregisterCommand(this.commandAdapter);
        if (this.getCoverageCommand() != null) {
            Command cmd = this.getCoverageCommand();
            CommandMap map = BukkitUtil.getAndFuckCommandMap();
            try {
                Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
                field.setAccessible(true);//让我访问!!
                Object o = field.get(map);
                ((HashMap) o).put(cmd.getName(), cmd);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Command getCoveredCommand() {
        return null;
    }

    public final Command getCoverageCommand() {
        return coverageCommand;
    }

    public abstract boolean onCommand(CommandSender sender, String[] args);

    public abstract void onCommandTab(CommandSender sender, String[] args, List<String> tabList);

    public void sendExceptionMessage(CommandSender sender) {
        this.commandAdapter.sendExceptionMessage(sender);
    }

    public static final class AdapterCommand<T extends CommandModule> extends ModuleCommand<T> {
        private AdapterCommand(T module) {
            super(module);
        }

        public boolean isOP() {
            return this.getModule().getClass().getAnnotation(QuarkCommand.class).op();
        }

        public @NotNull String getName() {
            return this.getModule().getClass().getAnnotation(QuarkCommand.class).name();
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if(this.getModule().onCommand(sender, args)){
                return;
            }
            if (this.getModule().getCoverageCommand() == null) {
                return;
            }
            this.getModule().getCoverageCommand().execute(sender, this.getLabel(), args);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
            this.getModule().onCommandTab(sender, args, tabList);
            if (this.getModule().getCoverageCommand() == null) {
                return;
            }
            tabList.addAll(this.getModule().getCoverageCommand().tabComplete(sender, this.getLabel(), args));
        }
    }
}
