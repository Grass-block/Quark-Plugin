package org.tbstcraft.quark.automatic;

import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.command.CommandRegistry;
import org.tbstcraft.quark.command.ModuleCommand;
import org.tbstcraft.quark.command.QuarkCommand;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.service.task.TaskService;

@CommandRegistry({VMGarbageCleaner.GCCommand.class})
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
            this.getLanguage().broadcastMessage(true, "gc-start");
        }
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        if (this.getConfig().getBoolean("broadcast")) {
            this.getLanguage().broadcastMessage(true, "gc-end", collect);
        }
    }

    public void manualGC(CommandSender sender) {
        this.getLanguage().sendMessageTo(sender, "gc-start");
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        this.getLanguage().sendMessageTo(sender, "gc-end", collect);
    }


    @QuarkCommand(name = "gc", op = true)
    public static final class GCCommand extends ModuleCommand<VMGarbageCleaner> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().manualGC(sender);
        }
    }
}
