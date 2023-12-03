package org.tbstcraft.quark.display;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.module.PluginModule;
import org.tbstcraft.quark.module.QuarkModule;
import org.tbstcraft.quark.util.BukkitUtil;
import org.tbstcraft.quark.util.registry.TypeItem;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

@Deprecated
@QuarkModule
public final class ChatHint extends PluginModule {
    private BukkitRunnable runnable;
    private int index;

    @Override
    public void onEnable() {
        this.runnable = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        this.tick();
        int p = this.getConfig().getInt("period");
        this.runnable.runTaskTimer(Quark.PLUGIN, p, p);
    }

    @Override
    public void onDisable() {
        this.runnable.cancel();
    }

    public void tick() {
        this.index++;
        //todo
    }
}
