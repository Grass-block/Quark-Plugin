package org.atcraftmc.quark.contents;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.modular.Registrations;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import me.gb2022.modular.module.ApplicationModule;

@AutoRegister(Registrations.SERVER_EVENT)
@ApplicationModule(version = "0.3",beta = true)
public class ElytraAeronautics extends SLPackageModule {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
        player.setVelocity(player.getLocation().getDirection().multiply(10));
    }
}
