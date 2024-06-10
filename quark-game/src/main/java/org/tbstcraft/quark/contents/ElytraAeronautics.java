package org.tbstcraft.quark.contents;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import me.gb2022.commons.reflect.AutoRegister;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(version = "0.3",beta = true)
public class ElytraAeronautics extends PackageModule {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
        player.setVelocity(player.getLocation().getDirection().multiply(10));
    }
}
