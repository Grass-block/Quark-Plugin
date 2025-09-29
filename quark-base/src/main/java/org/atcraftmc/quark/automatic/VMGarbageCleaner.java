package org.atcraftmc.quark.automatic;

import me.gb2022.commons.reflect.Inject;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.command.CommandSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;

@CommandProvider({VMGarbageCleaner.GCCommand.class})
@ApplicationModule(version = "1.3.0")
public final class VMGarbageCleaner extends SLPackageModule {
    public static final String GC_TASK_TID = "quark:auto_gc:gc";

    @Inject
    private LanguageEntry language;

    @Override
    public void enable() {
        int period = ConfigAccessor.getInt(this.config(), "period");
        TaskService.async().timer(GC_TASK_TID, period, period, this::gc);
    }

    @Override
    public void disable() {
        TaskService.async().cancel(GC_TASK_TID);
    }

    public void gc() {
        if (ConfigAccessor.getBool(this.config(), "broadcast")) {
            MessageAccessor.broadcast(this.language, true, false, "gc-start");
        }
        long prev = Runtime.getRuntime().freeMemory();
        System.gc();
        long now = Runtime.getRuntime().freeMemory();
        long collect = (now - prev) / 1048576;
        if (ConfigAccessor.getBool(this.config(), "broadcast")) {
            MessageAccessor.broadcast(this.language, true, false, "gc-end", collect);
        }
    }

    public void manualGC(CommandSender sender) {
        MessageAccessor.send(this.language, sender, "gc-start");

        var prev = Runtime.getRuntime().freeMemory();
        System.gc();
        var now = Runtime.getRuntime().freeMemory();
        var collect = (now - prev) / 1048576;

        MessageAccessor.send(this.language, sender, "gc-end", collect);
    }


    @QuarkCommand(name = "gc", permission = "-quark.gc.command")
    public static final class GCCommand extends ModuleCommand<VMGarbageCleaner> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            this.getModule().manualGC(sender);
        }
    }
}
