package org.atcraftmc.starlight.display;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.gb2022.modular.APIIncompatibleException;
import org.atcraftmc.starlight.foundation.platform.Compatibility;
import org.atcraftmc.qlib.texts.TextBuilder;
import me.gb2022.modular.module.ApplicationModule;

import java.util.Objects;

@ApplicationModule(id="custom-death-message")
@AutoRegister(Registrations.SERVER_EVENT)
public final class CustomDeathMessage extends SLPackageModule {
    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        Compatibility.requireMethod(()->PlayerDeathEvent.class.getMethod("deathMessage"));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        var prefix = TextBuilder.buildComponent(config().value("prefix").string());
        var suffix = TextBuilder.buildComponent(config().value("suffix").string());

        if (e.deathMessage() == null) {
            return;
        }

        e.deathMessage(prefix.append(Objects.requireNonNull(e.deathMessage())).append(suffix));
    }
}
