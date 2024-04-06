package org.tbstcraft.quark.contents;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.tbstcraft.quark.module.services.EventListener;
import org.tbstcraft.quark.module.PackageModule;
import org.tbstcraft.quark.module.QuarkModule;

@EventListener
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
