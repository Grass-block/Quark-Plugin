package org.atcraftmc.quark.utilities;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.foundation.platform.Players;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

@QuarkModule(version = "1.0")
@QuarkCommand(name = "align", playerOnly = true)
public final class PositionAlign extends CommandModule {

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();

        Location loc = new Location(player.getWorld(), px + 0.5, py, pz + 0.5);
        loc.setYaw(getYaw(player));
        loc.setPitch(0);
        Players.teleport(player, loc);

        getLanguage().sendMessage(sender, "align", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw(), loc.getPitch());
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
