package org.atcraftmc.quark.display;

import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.atcraftmc.starlight.foundation.platform.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;

import java.util.Objects;

@SLModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CustomDeathMessage extends PackageModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(()->PlayerDeathEvent.class.getMethod("deathMessage"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        var prefix = TextBuilder.buildComponent(getConfig().value("prefix").string());
        var suffix = TextBuilder.buildComponent(getConfig().value("suffix").string());

        if (e.deathMessage() == null) {
            return;
        }

        e.deathMessage(prefix.append(Objects.requireNonNull(e.deathMessage())).append(suffix));
    }
}
