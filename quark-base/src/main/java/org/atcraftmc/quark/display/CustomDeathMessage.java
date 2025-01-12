package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.tbstcraft.quark.foundation.platform.APIIncompatibleException;
import org.tbstcraft.quark.foundation.platform.Compatibility;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.Objects;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CustomDeathMessage extends PackageModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(()->PlayerDeathEvent.class.getMethod("deathMessage"));
    }

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
