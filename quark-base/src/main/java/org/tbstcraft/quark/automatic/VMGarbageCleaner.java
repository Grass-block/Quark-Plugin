package org.tbstcraft.quark.automatic;

import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

@CommandProvider({VMGarbageCleaner.GCCommand.class})
@QuarkModule(version = "1.3.0", defaultEnable = false)//no needed
public final class VMGarbageCleaner extends PackageModule {
    public static final String GC_TASK_TID = "quark:auto_gc:gc";

    @Inject
    private LanguageEntry language;

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
            this.language.broadcastMessage(true, false, "gc-start");
        }
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        if (this.getConfig().getBoolean("broadcast")) {
            this.language.broadcastMessage(true, false, "gc-end", collect);
        }
    }

    public void manualGC(CommandSender sender) {
        this.language.sendMessage(sender, "gc-start");

        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;

        this.language.sendMessage(sender, "gc-end", collect);
    }


    @QuarkCommand(name = "gc", permission = "-quark.gc.command")
    public static final class GCCommand extends ModuleCommand<VMGarbageCleaner> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().manualGC(sender);
        }
    }
}
