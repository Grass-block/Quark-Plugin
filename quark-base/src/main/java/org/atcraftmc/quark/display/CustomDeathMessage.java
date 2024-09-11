package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.tbstcraft.quark.foundation.text.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Objects;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CustomDeathMessage extends PackageModule {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        var prefix = TextBuilder.buildComponent(getConfig().getString("prefix"));
        var suffix = TextBuilder.buildComponent(getConfig().getString("suffix"));

        if (e.deathMessage() == null) {
            return;
        }

        e.deathMessage(prefix.append(Objects.requireNonNull(e.deathMessage())).append(suffix));
    }
}