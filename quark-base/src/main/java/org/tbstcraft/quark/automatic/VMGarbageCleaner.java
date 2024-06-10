package org.tbstcraft.quark.automatic;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.service.base.task.TaskService;

@CommandProvider({VMGarbageCleaner.GCCommand.class})
@QuarkModule(version = "1.3.0")
public final class VMGarbageCleaner extends PackageModule {
    public static final String GC_TASK_TID = "quark:auto_gc:gc";

    @Override
    public void enable() {
        int period = getConfig().getInt("period");
        TaskService.asyncTimerTask(GC_TASK_TID, period, period, this::gc);
    }

    @Override
    public void disable() {
        TaskService.cancelTask(GC_TASK_TID);
    }

    public void gc() {
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true,false, "gc-start");
        }
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true,false, "gc-end", collect);
        }
    }

    public void manualGC(CommandSender sender) {
        this.getLanguage().sendMessage(sender, "gc-start");
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        this.getLanguage().sendMessage(sender, "gc-end", collect);
    }


    @QuarkCommand(name = "gc", permission = "-quark.gc.command")
    public static final class GCCommand extends ModuleCommand<VMGarbageCleaner> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().manualGC(sender);
        }
    }
}
