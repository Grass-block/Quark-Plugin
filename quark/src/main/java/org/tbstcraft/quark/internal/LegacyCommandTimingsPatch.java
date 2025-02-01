package org.tbstcraft.quark.internal;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.LegacyCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

@SuppressWarnings("removal")//legacy compat
@QuarkModule(internal = true)
@AutoRegister({ServiceType.EVENT_LISTEN})
public final class LegacyCommandTimingsPatch extends PackageModule {

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.assertion(Bukkit.getServer().getVersion().contains("PaperSpigot"));
        Compatibility.requireClass(() -> Class.forName("co.aikar.timings.TimingsManager"));
    }

    @Override
    public void enable() {
        this.inject();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        this.inject();
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        this.inject();
    }

    public void inject() {
        for (var c : LegacyCommandManager.getKnownCommands(LegacyCommandManager.getCommandMap()).values()) {
            if (c.timings == null) {
                c.timings = co.aikar.timings.TimingsManager.getCommandTiming("_quark_inject", c);
            }
        }
    }
}
