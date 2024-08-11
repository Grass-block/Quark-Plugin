package org.tbstcraft.quark.internal;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.util.Comments;

import java.util.List;

@QuarkModule
@Comments("FUCK YOU!!!!!!!!!")
public final class CounterPluginConflictHandler extends PackageModule {
    public static final String MAIN_CLASS = "org.kyoikumi.plugin.counter.Counter";
    public static final String PLUGIN_ID = "Counter";
    public static final List<String> CONFLICT_LIST = List.of("quark-display","quark-chat");

    @Override
    public void enable() {
        Plugin counter = Bukkit.getPluginManager().getPlugin(PLUGIN_ID);

        if (counter == null) {
            return;
        }
        if (!counter.getClass().getName().equals(MAIN_CLASS)) {
            return;
        }

        getLogger().severe("detected counter plugin, this may cause conflict.");
        getLogger().severe("we WON'T fix any problem of duplicated function.");

        for (String s : CONFLICT_LIST) {
            getLogger().severe("rejected local package %s.".formatted(s));
            PackageManager.addRejection(s);
        }
    }
}
