package org.atcraftmc.starlight.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.starlight.foundation.platform.Players;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

@SLModule
@QuarkCommand(name = "align", playerOnly = true, permission = "+starlight.command.align")
@AutoRegister(Registers.BUKKIT_EVENT)
public final class PositionAlign extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        var player = (Player) sender;

        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();

        var loc = new Location(player.getWorld(), px + 0.5, py, pz + 0.5);
        loc.setYaw(getYaw(player));
        loc.setPitch(0);
        Players.teleport(player, loc);

        MessageAccessor.send(
                this.getLanguage(),
                sender,
                "align",
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (getConfig().value("fix-spawn-position").bool()) {
            var p = event.getPlayer().getWorld().getSpawnLocation();
            var rp = event.getRespawnLocation();
            if (p.getX() == rp.getX() && p.getY() == rp.getY() && p.getZ() == rp.getZ()) {
                event.setRespawnLocation(event.getRespawnLocation().add(0.5, 0, 0.5));
            }
        }
    }

    public int getYaw(Player player) {
        // Get the player's yaw angle
        float yaw = player.getLocation().getYaw();

        // Normalize yaw to be between 0 and 360 degrees
        if (yaw < 0) {
            yaw += 360;
        }

        // Map yaw to one of the 8 directions
        if (yaw >= 337.5 || yaw < 22.5) {
            return 0;
        } else if (yaw >= 22.5 && yaw < 67.5) {
            return 45;
        } else if (yaw >= 67.5 && yaw < 112.5) {
            return 90;
        } else if (yaw >= 112.5 && yaw < 157.5) {
            return 135;
        } else if (yaw >= 157.5 && yaw < 202.5) {
            return 180;
        } else if (yaw >= 202.5 && yaw < 247.5) {
            return 225;
        } else if (yaw >= 247.5 && yaw < 292.5) {
            return 270;
        } else if (yaw >= 292.5 && yaw < 337.5) {
            return 315;
        }

        return 0;
    }
}
