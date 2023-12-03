package org.tbstcraft.quark.misc;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.AbstractCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.command.CommandManager;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.TaskManager;

import java.util.List;

@QuarkModule
public final class AutoGC extends PluginModule {
    public static final String GC_TASK_TID = "quark_misc:auto_gc:gc";
    private final AbstractCommand command = new CommandHandler(this);

    @Override
    public void onEnable() {
        this.registerCommand(this.command);
        int period = getConfig().getInt("period");
        TaskManager.runTimer(GC_TASK_TID, period, period, this::gc);
    }

    @Override
    public void onDisable() {
        CommandManager.unregisterCommand(this.command);
        TaskManager.cancelTask(GC_TASK_TID);
    }

    public void gc() {
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true, "gc_start");
        }
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true, "gc_end", collect);
        }
    }

    public void manualGC(CommandSender sender) {
        this.getLanguage().sendMessageTo(sender, "gc_start");
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        this.getLanguage().sendMessageTo(sender, "gc_end", collect);
    }


    @QuarkCommand(name = "gc", op = true)
    public static final class CommandHandler extends ModuleCommand<AutoGC> {
        public CommandHandler(AutoGC module) {
            super(module);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().manualGC(sender);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] args, List<String> tabList) {
        }
    }
}
