package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.packages.PackageManager;

import java.util.List;

@QuarkModule(version = "1.0.3")
public final class CounterPluginConflictHandler extends PackageModule {
    public static final String MAIN_CLASS = "org.kyoikumi.plugin.counter.Counter";
    public static final String PLUGIN_ID = "Counter";
    public static final List<String> CONFLICT_LIST = List.of("quark-display", "quark-chat");

    @Inject
    public Logger logger;

    @Override
    public void enable() {
        Plugin counter = Bukkit.getPluginManager().getPlugin(PLUGIN_ID);

        if (counter == null) {
            return;
        }
        if (!counter.getClass().getName().equals(MAIN_CLASS)) {
            return;
        }

        this.logger.warn("detected 'counter' plugin by 'org.kyoikumi', this may cause conflict.");
        this.logger.warn("we WON'T fix any problem of duplicated function.");

        for (String s : CONFLICT_LIST) {
            this.logger.warn("rejected local package %s.".formatted(s));
            PackageManager.addRejection(s);
        }
    }
}
